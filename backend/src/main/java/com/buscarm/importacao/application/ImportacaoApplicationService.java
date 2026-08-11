package com.buscarm.importacao.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.buscarm.auditoria.application.port.in.RegistrarAuditoriaUseCase;
import com.buscarm.importacao.application.port.in.ConfirmarImagemUseCase;
import com.buscarm.importacao.application.port.in.ConfirmarImportacaoUseCase;
import com.buscarm.importacao.application.port.in.ConsultarImportacaoUseCase;
import com.buscarm.importacao.application.port.in.ProcessarImagemUseCase;
import com.buscarm.importacao.application.port.in.ValidarPlanilhaUseCase;
import com.buscarm.importacao.application.port.out.ExtratorImagemPort;
import com.buscarm.importacao.application.port.out.ImportacaoRepositoryPort;
import com.buscarm.importacao.application.port.out.LeitorPlanilhaPort;
import com.buscarm.importacao.domain.Importacao;
import com.buscarm.importacao.domain.ItemImportacao;
import com.buscarm.importacao.domain.RegistroExtraido;
import com.buscarm.importacao.domain.StatusItem;
import com.buscarm.importacao.domain.TipoImportacao;
import com.buscarm.importacao.domain.ValidadorRegistro;
import com.buscarm.importacao.domain.exception.ImportacaoNaoEncontradaException;
import com.buscarm.registro.application.port.in.BuscarRegistrosUseCase;
import com.buscarm.registro.application.port.in.CadastrarRegistroUseCase;
import com.buscarm.registro.application.port.in.CadastrarRegistroUseCase.CadastrarRegistroCommand;
import com.buscarm.registro.domain.FiltroRegistros;
import com.buscarm.registro.domain.NomeNormalizer;
import com.buscarm.registro.domain.Registro;
import com.buscarm.registro.domain.StatusValidacao;
import com.buscarm.registro.domain.exception.RegistroDuplicadoException;

/**
 * Orquestra o pipeline único de importação (ADR-0003): extrair -> classificar -> pré-visualizar
 * e, depois, gravar só os aprovados. Vale para planilha (POI) e para foto (OCR) — mesma
 * classificação e mesma gravação; muda só o extrator de entrada.
 */
@Service
public class ImportacaoApplicationService implements ValidarPlanilhaUseCase, ConfirmarImportacaoUseCase,
        ConsultarImportacaoUseCase, ProcessarImagemUseCase, ConfirmarImagemUseCase {

    private final LeitorPlanilhaPort leitor;
    private final ExtratorImagemPort extratorImagem;
    private final ImportacaoRepositoryPort repositorio;
    private final BuscarRegistrosUseCase buscarRegistros;
    private final CadastrarRegistroUseCase cadastrarRegistro;
    private final RegistrarAuditoriaUseCase registrarAuditoria;

    public ImportacaoApplicationService(LeitorPlanilhaPort leitor,
                                        ExtratorImagemPort extratorImagem,
                                        ImportacaoRepositoryPort repositorio,
                                        BuscarRegistrosUseCase buscarRegistros,
                                        CadastrarRegistroUseCase cadastrarRegistro,
                                        RegistrarAuditoriaUseCase registrarAuditoria) {
        this.leitor = leitor;
        this.extratorImagem = extratorImagem;
        this.repositorio = repositorio;
        this.buscarRegistros = buscarRegistros;
        this.cadastrarRegistro = cadastrarRegistro;
        this.registrarAuditoria = registrarAuditoria;
    }

    @Override
    @Transactional
    public Importacao validar(String nomeArquivo, byte[] conteudo) {
        Importacao importacao = Importacao.validada(
                TipoImportacao.PLANILHA, nomeArquivo, classificar(leitor.ler(conteudo)));
        return repositorio.salvar(importacao);
    }

    @Override
    @Transactional
    public Importacao processar(String nomeArquivo, byte[] imagem) {
        Importacao importacao = Importacao.validada(
                TipoImportacao.IMAGEM, nomeArquivo, classificar(extratorImagem.extrair(imagem)));
        return repositorio.salvar(importacao);
    }

    @Override
    @Transactional
    public Importacao confirmar(long importacaoId, List<Integer> linhasAprovadas) {
        Importacao importacao = carregar(importacaoId);
        Set<Integer> aprovadas = new HashSet<>(linhasAprovadas);
        for (ItemImportacao item : importacao.getItens()) {
            if (!item.isValido()) {
                continue; // INVALIDO/DUPLICADO/CONFLITO: preserva a classificação
            }
            if (!aprovadas.contains(item.getLinhaOrigem())) {
                item.marcarIgnorado("Não aprovado na confirmação");
                continue;
            }
            importar(item, item.getRmExtraido(), item.getNomeExtraido());
        }
        return finalizar(importacao, "importados");
    }

    @Override
    @Transactional
    public Importacao confirmarImagem(long importacaoId, List<ItemCorrigido> itens) {
        Importacao importacao = carregar(importacaoId);
        Map<Integer, ItemCorrigido> porLinha = new HashMap<>();
        for (ItemCorrigido corr : itens) {
            porLinha.put(corr.linha(), corr);
        }
        for (ItemImportacao item : importacao.getItens()) {
            ItemCorrigido corr = porLinha.get(item.getLinhaOrigem());
            if (corr == null) {
                continue; // linha não aprovada na conferência
            }
            if (corr.rm() == null || corr.nomeCompleto() == null || corr.nomeCompleto().isBlank()) {
                item.marcarIgnorado("Dados incompletos na conferência");
                continue;
            }
            importar(item, corr.rm(), corr.nomeCompleto());
        }
        return finalizar(importacao, "importados (imagem)");
    }

    @Override
    @Transactional(readOnly = true)
    public Importacao buscarPorId(long id) {
        return carregar(id);
    }

    // --- helpers do pipeline ---

    private List<ItemImportacao> classificar(List<RegistroExtraido> extraidos) {
        Map<Integer, String> vistosNoLote = new HashMap<>();
        List<ItemImportacao> itens = new ArrayList<>();
        for (RegistroExtraido extraido : extraidos) {
            Optional<String> nomeExistente = Optional.empty();
            if (extraido.rm() != null) {
                nomeExistente = vistosNoLote.containsKey(extraido.rm())
                        ? Optional.of(vistosNoLote.get(extraido.rm()))
                        : nomeBuscaCadastrado(extraido.rm());
            }
            ValidadorRegistro.Resultado r = ValidadorRegistro.classificar(extraido, nomeExistente);
            itens.add(ItemImportacao.novo(extraido, r.status(), r.mensagem()));
            if (r.status() == StatusItem.VALIDO) {
                vistosNoLote.put(extraido.rm(), NomeNormalizer.normalizar(extraido.nomeCompleto()));
            }
        }
        return itens;
    }

    private void importar(ItemImportacao item, Integer rm, String nomeCompleto) {
        try {
            Registro registro = cadastrarRegistro.cadastrar(
                    new CadastrarRegistroCommand(rm, nomeCompleto, null, StatusValidacao.PENDENTE));
            item.marcarImportado(registro.getId());
        } catch (RegistroDuplicadoException e) {
            item.marcarIgnorado("RM já cadastrado");
        }
    }

    private Importacao finalizar(Importacao importacao, String rotulo) {
        importacao.confirmar();
        Importacao salva = repositorio.salvar(importacao);
        registrarAuditoria.registrar("IMPORTAR", "IMPORTACAO", salva.getId(),
                salva.contar(StatusItem.IMPORTADO) + " " + rotulo);
        return salva;
    }

    private Importacao carregar(long id) {
        return repositorio.buscarPorId(id)
                .orElseThrow(() -> new ImportacaoNaoEncontradaException(id));
    }

    private Optional<String> nomeBuscaCadastrado(int rm) {
        return buscarRegistros.pesquisar(FiltroRegistros.porRm(rm), 0, 1).conteudo().stream()
                .findFirst()
                .map(Registro::getNomeBusca);
    }
}
