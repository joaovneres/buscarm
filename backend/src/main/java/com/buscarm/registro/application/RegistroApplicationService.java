package com.buscarm.registro.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.buscarm.auditoria.application.port.in.RegistrarAuditoriaUseCase;
import com.buscarm.registro.application.port.in.BuscarRegistrosUseCase;
import com.buscarm.registro.application.port.in.CadastrarRegistroUseCase;
import com.buscarm.registro.application.port.out.RegistroRepositoryPort;
import com.buscarm.registro.application.port.out.UsuarioSessaoPort;
import com.buscarm.shared.UsuarioAtualPort;
import com.buscarm.registro.domain.Registro;
import com.buscarm.registro.domain.NomeNormalizer;
import com.buscarm.registro.domain.exception.RegistroDuplicadoException;
import com.buscarm.registro.domain.exception.RegistroNaoEncontradoException;
import com.buscarm.shared.Pagina;

/**
 * Serviço de aplicação (hexagonal): implementa as portas de entrada orquestrando as portas de
 * saída. Não conhece web nem JPA — só domínio e portas.
 */
@Service
public class RegistroApplicationService implements CadastrarRegistroUseCase, BuscarRegistrosUseCase {

    private final RegistroRepositoryPort repositorio;
    private final UsuarioAtualPort usuarioAtual;
    private final UsuarioSessaoPort usuarioSessao;
    private final RegistrarAuditoriaUseCase registrarAuditoria;

    public RegistroApplicationService(RegistroRepositoryPort repositorio,
                                      UsuarioAtualPort usuarioAtual,
                                      UsuarioSessaoPort usuarioSessao,
                                      RegistrarAuditoriaUseCase registrarAuditoria) {
        this.repositorio = repositorio;
        this.usuarioAtual = usuarioAtual;
        this.usuarioSessao = usuarioSessao;
        this.registrarAuditoria = registrarAuditoria;
    }

    @Override
    @Transactional
    public Registro cadastrar(CadastrarRegistroCommand comando) {
        usuarioSessao.definirUsuarioSessao(usuarioAtual.getUsuarioAtual());
        if (repositorio.existePorRm(comando.rm())) {
            throw new RegistroDuplicadoException(comando.rm());
        }
        Registro salvo = repositorio.salvar(
                Registro.novo(comando.rm(), comando.nomeCompleto(), comando.anoRegistro()));
        registrarAuditoria.registrar("CRIAR", "REGISTRO", salvo.getId(), "RM " + salvo.getRm());
        return salvo;
    }

    @Override
    @Transactional(readOnly = true)
    public Registro buscarPorId(long id) {
        return repositorio.buscarPorId(id)
                .orElseThrow(() -> new RegistroNaoEncontradoException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Pagina<Registro> pesquisar(String nome, Integer rm, int pagina, int tamanho) {
        if (rm != null) {
            List<Registro> encontrados = repositorio.buscarPorRmAtivo(rm)
                    .map(List::of)
                    .orElseGet(List::of);
            return new Pagina<>(encontrados, 0, encontrados.size(), encontrados.size(),
                    encontrados.isEmpty() ? 0 : 1);
        }
        if (nome != null && !nome.isBlank()) {
            return repositorio.buscarPorNome(NomeNormalizer.normalizar(nome), pagina, tamanho);
        }
        return repositorio.listarAtivos(pagina, tamanho);
    }
}
