package com.buscarm.registro.domain.exception;

/** Registro inexistente. */
public class RegistroNaoEncontradoException extends RuntimeException {
    public RegistroNaoEncontradoException(long id) {
        super("Registro " + id + " não encontrado");
    }
}
