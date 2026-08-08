package com.buscarm.registro.domain;

import java.text.Normalizer;

/**
 * Regra de domínio: normalização de nomes para busca acento/caixa-insensível e extração do
 * sobrenome a partir do formato "Sobrenome, Restante do nome" usado no livro de RM.
 */
public final class NomeNormalizer {

    private NomeNormalizer() {
    }

    /** minúsculas, sem acentos, espaços colapsados — base da busca por nome. */
    public static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return semAcento.trim().toLowerCase().replaceAll("\\s+", " ");
    }

    /** parte antes da primeira vírgula; se não houver vírgula, o nome inteiro. */
    public static String extrairSobrenome(String nomeCompleto) {
        if (nomeCompleto == null || nomeCompleto.isBlank()) {
            return "";
        }
        int virgula = nomeCompleto.indexOf(',');
        String sobrenome = virgula >= 0 ? nomeCompleto.substring(0, virgula) : nomeCompleto;
        return sobrenome.trim();
    }
}
