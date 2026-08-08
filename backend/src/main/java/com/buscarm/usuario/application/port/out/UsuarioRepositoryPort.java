package com.buscarm.usuario.application.port.out;

import java.util.Optional;

import com.buscarm.usuario.domain.Usuario;

/** Porta de saída: persistência de usuários. */
public interface UsuarioRepositoryPort {

    Optional<Usuario> buscarPorEmail(String email);

    boolean existeAlgum();

    Usuario salvar(Usuario usuario);
}
