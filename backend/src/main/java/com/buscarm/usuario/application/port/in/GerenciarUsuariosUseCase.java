package com.buscarm.usuario.application.port.in;

import java.util.List;

import com.buscarm.usuario.domain.Papel;
import com.buscarm.usuario.domain.Usuario;

/** Porta de entrada: gestão de usuários pelo administrador. */
public interface GerenciarUsuariosUseCase {

    Usuario criar(CriarUsuarioCommand comando);

    List<Usuario> listar();

    /** Admin redefine a senha de um usuário (senha provisória: ele deverá trocá-la depois). */
    void resetarSenha(long usuarioId, String novaSenha);

    /** Comando de criação (independente do DTO web). */
    record CriarUsuarioCommand(String nome, String username, String senha, Papel papel) {
    }
}
