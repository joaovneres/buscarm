package com.buscarm.usuario.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.buscarm.auditoria.application.port.in.RegistrarAuditoriaUseCase;
import com.buscarm.usuario.application.port.in.GerenciarUsuariosUseCase;
import com.buscarm.usuario.application.port.in.TrocarSenhaUseCase;
import com.buscarm.usuario.application.port.out.CodificadorSenhaPort;
import com.buscarm.usuario.application.port.out.UsuarioRepositoryPort;
import com.buscarm.usuario.domain.Usuario;
import com.buscarm.usuario.domain.exception.CredenciaisInvalidasException;
import com.buscarm.usuario.domain.exception.UsernameJaCadastradoException;
import com.buscarm.usuario.domain.exception.UsuarioNaoEncontradoException;

/** Gestão de usuários: criar/listar/reset (ADMIN) e troca da própria senha (autosserviço). */
@Service
public class UsuarioAdminService implements GerenciarUsuariosUseCase, TrocarSenhaUseCase {

    private final UsuarioRepositoryPort repositorio;
    private final CodificadorSenhaPort codificador;
    private final RegistrarAuditoriaUseCase registrarAuditoria;

    public UsuarioAdminService(UsuarioRepositoryPort repositorio,
                               CodificadorSenhaPort codificador,
                               RegistrarAuditoriaUseCase registrarAuditoria) {
        this.repositorio = repositorio;
        this.codificador = codificador;
        this.registrarAuditoria = registrarAuditoria;
    }

    @Override
    @Transactional
    public Usuario criar(CriarUsuarioCommand comando) {
        String username = comando.username().trim().toLowerCase();
        if (repositorio.buscarPorUsername(username).isPresent()) {
            throw new UsernameJaCadastradoException(username);
        }
        Usuario salvo = repositorio.salvar(Usuario.novo(
                comando.nome(), username, codificador.codificar(comando.senha()), comando.papel()));
        registrarAuditoria.registrar("CRIAR_USUARIO", "USUARIO", salvo.getId(),
                salvo.getUsername() + " (" + salvo.getPapel() + ")");
        return salvo;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return repositorio.listarTodos();
    }

    @Override
    @Transactional
    public void resetarSenha(long usuarioId, String novaSenha) {
        Usuario usuario = repositorio.buscarPorId(usuarioId)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(usuarioId));
        repositorio.atualizar(usuario.comSenha(codificador.codificar(novaSenha), true));
        registrarAuditoria.registrar("RESETAR_SENHA", "USUARIO", usuarioId, usuario.getUsername());
    }

    @Override
    @Transactional
    public void trocarSenha(String username, String senhaAtual, String novaSenha) {
        Usuario usuario = repositorio.buscarPorUsername(username == null ? "" : username.trim().toLowerCase())
                .filter(Usuario::isAtivo)
                .orElseThrow(CredenciaisInvalidasException::new);
        if (!codificador.confere(senhaAtual, usuario.getSenhaHash())) {
            throw new CredenciaisInvalidasException();
        }
        repositorio.atualizar(usuario.comSenha(codificador.codificar(novaSenha), false));
        registrarAuditoria.registrar("TROCAR_SENHA", "USUARIO", usuario.getId(), usuario.getUsername());
    }
}
