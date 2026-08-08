package com.buscarm.registro.domain.exception;

/** RM já cadastrado — viola a regra de unicidade do índice. */
public class RegistroDuplicadoException extends RuntimeException {
    public RegistroDuplicadoException(int rm) {
        super("RM " + rm + " já cadastrado");
    }
}
