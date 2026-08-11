package com.buscarm.usuario.domain.exception;

/** Usuário inexistente. */
public class UsuarioNaoEncontradoException extends RuntimeException {
    public UsuarioNaoEncontradoException(long id) {
        super("Usuário " + id + " não encontrado");
    }
}
