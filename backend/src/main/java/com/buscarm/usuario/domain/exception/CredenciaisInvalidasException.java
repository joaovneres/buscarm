package com.buscarm.usuario.domain.exception;

/** E-mail/senha inválidos. */
public class CredenciaisInvalidasException extends RuntimeException {
    public CredenciaisInvalidasException() {
        super("E-mail ou senha inválidos");
    }
}
