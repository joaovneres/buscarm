package com.buscarm.comum;

/** Violação de regra de negócio (ex.: RM já cadastrado) -> 409. */
public class RegraNegocioException extends RuntimeException {
    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
