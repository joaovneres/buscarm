package com.buscarm.usuario.application.port.out;

import java.util.List;
import java.util.Optional;

import com.buscarm.usuario.domain.Usuario;

/** Porta de saída: persistência de usuários. */
public interface UsuarioRepositoryPort {

    Optional<Usuario> buscarPorUsername(String username);

    Optional<Usuario> buscarPorId(long id);

    boolean existeAlgum();

    Usuario salvar(Usuario usuario);

    /** Atualiza a senha (hash) e o flag de senha provisória de um usuário existente. */
    Usuario atualizar(Usuario usuario);

    /** Todos os usuários, ordenados por nome (para a gestão pelo admin). */
    List<Usuario> listarTodos();
}
