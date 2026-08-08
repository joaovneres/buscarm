package com.buscarm.importacao.domain;

import java.util.Optional;

import com.buscarm.registro.domain.NomeNormalizer;

/**
 * Regra ÚNICA de validação de um registro extraído (ADR-0003) — vale para planilha e para OCR.
 * Pura: recebe o registro extraído e, se existir um RM igual, o nome normalizado do já cadastrado.
 */
public final class ValidadorRegistro {

    private ValidadorRegistro() {
    }

    public record Resultado(StatusItem status, String mensagem) {
    }

    /**
     * @param extraido            linha extraída da fonte
     * @param nomeBuscaExistente  nome normalizado de um registro já existente com o mesmo RM
     *                            (no banco ou antes no mesmo lote), se houver
     */
    public static Resultado classificar(RegistroExtraido extraido, Optional<String> nomeBuscaExistente) {
        if (extraido.rm() == null) {
            return new Resultado(StatusItem.INVALIDO, "RM ausente");
        }
        if (extraido.nomeCompleto() == null || extraido.nomeCompleto().isBlank()) {
            return new Resultado(StatusItem.INVALIDO, "Nome ausente");
        }
        if (nomeBuscaExistente.isPresent()) {
            String atual = NomeNormalizer.normalizar(extraido.nomeCompleto());
            if (atual.equals(nomeBuscaExistente.get())) {
                return new Resultado(StatusItem.DUPLICADO, "RM já cadastrado");
            }
            return new Resultado(StatusItem.CONFLITO, "RM já usado por outro nome — revisão necessária");
        }
        return new Resultado(StatusItem.VALIDO, null);
    }
}
