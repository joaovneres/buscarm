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

import com.buscarm.importacao.application.port.in.ConfirmarImportacaoUseCase;
import com.buscarm.importacao.application.port.in.ConsultarImportacaoUseCase;
import com.buscarm.importacao.application.port.in.ValidarPlanilhaUseCase;
import com.buscarm.importacao.application.port.out.ImportacaoRepositoryPort;
import com.buscarm.importacao.application.port.out.LeitorPlanilhaPort;
import com.buscarm.importacao.domain.Importacao;
import com.buscarm.importacao.domain.ItemImportacao;
import com.buscarm.importacao.domain.RegistroExtraido;
import com.buscarm.importacao.domain.TipoImportacao;
import com.buscarm.importacao.domain.ValidadorRegistro;
import com.buscarm.importacao.domain.exception.ImportacaoNaoEncontradaException;
import com.buscarm.registro.application.port.in.BuscarRegistrosUseCase;
import com.buscarm.registro.application.port.in.CadastrarRegistroUseCase;
import com.buscarm.registro.application.port.in.CadastrarRegistroUseCase.CadastrarRegistroCommand;
import com.buscarm.registro.domain.NomeNormalizer;
import com.buscarm.registro.domain.Registro;
import com.buscarm.registro.domain.exception.RegistroDuplicadoException;

/**
 * Orquestra o pipeline de importação por planilha (ADR-0003): extrair -> validar -> pré-visualizar
 * (validar) e, depois, gravar só os aprovados (confirmar). Reusa os casos de uso de `registro`
 * para consultar duplicidade e cadastrar — sem duplicar regra.
 */
@Service
public class ImportacaoApplicationService
        implements ValidarPlanilhaUseCase, ConfirmarImportacaoUseCase, ConsultarImportacaoUseCase {

    private final LeitorPlanilhaPort leitor;
    private final ImportacaoRepositoryPort repositorio;
    private final BuscarRegistrosUseCase buscarRegistros;
    private final CadastrarRegistroUseCase cadastrarRegistro;

    public ImportacaoApplicationService(LeitorPlanilhaPort leitor,
                                        ImportacaoRepositoryPort repositorio,
                                        BuscarRegistrosUseCase buscarRegistros,
                                        CadastrarRegistroUseCase cadastrarRegistro) {
        this.leitor = leitor;
        this.repositorio = repositorio;
        this.buscarRegistros = buscarRegistros;
        this.cadastrarRegistro = cadastrarRegistro;
    }

    @Override
    @Transactional
    public Importacao validar(String nomeArquivo, byte[] conteudo) {
        List<RegistroExtraido> extraidos = leitor.ler(conteudo);
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
            if (r.status() == com.buscarm.importacao.domain.StatusItem.VALIDO) {
                vistosNoLote.put(extraido.rm(), NomeNormalizer.normalizar(extraido.nomeCompleto()));
            }
        }

        Importacao importacao = Importacao.validada(TipoImportacao.PLANILHA, nomeArquivo, itens);
        return repositorio.salvar(importacao);
    }

    @Override
    @Transactional
    public Importacao confirmar(long importacaoId, List<Integer> linhasAprovadas) {
        Importacao importacao = repositorio.buscarPorId(importacaoId)
                .orElseThrow(() -> new ImportacaoNaoEncontradaException(importacaoId));
        Set<Integer> aprovadas = new HashSet<>(linhasAprovadas);

        for (ItemImportacao item : importacao.getItens()) {
            if (!item.isValido()) {
                continue; // INVALIDO/DUPLICADO/CONFLITO: preserva a classificação
            }
            if (!aprovadas.contains(item.getLinhaOrigem())) {
                item.marcarIgnorado("Não aprovado na confirmação");
                continue;
            }
            try {
                Registro registro = cadastrarRegistro.cadastrar(
                        new CadastrarRegistroCommand(item.getRmExtraido(), item.getNomeExtraido(), null));
                item.marcarImportado(registro.getId());
            } catch (RegistroDuplicadoException e) {
                item.marcarIgnorado("RM já cadastrado durante a confirmação");
            }
        }

        importacao.confirmar();
        return repositorio.salvar(importacao);
    }

    @Override
    @Transactional(readOnly = true)
    public Importacao buscarPorId(long id) {
        return repositorio.buscarPorId(id)
                .orElseThrow(() -> new ImportacaoNaoEncontradaException(id));
    }

    private Optional<String> nomeBuscaCadastrado(int rm) {
        return buscarRegistros.pesquisar(null, rm, 0, 1).conteudo().stream()
                .findFirst()
                .map(Registro::getNomeBusca);
    }
}
