package com.buscarm.importacao.domain.exception;

/** Importação inexistente. */
public class ImportacaoNaoEncontradaException extends RuntimeException {
    public ImportacaoNaoEncontradaException(long id) {
        super("Importação " + id + " não encontrada");
    }
}
