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

    /**
     * Deriva o sobrenome do nome completo (o "de-para"):
     * <ul>
     *   <li>Formato "Sobrenome, Nome" (com vírgula): parte antes da vírgula (compatibilidade).</li>
     *   <li>Nome natural ("Nome ... Sobrenome"): a última palavra é o sobrenome.</li>
     * </ul>
     */
    public static String extrairSobrenome(String nomeCompleto) {
        if (nomeCompleto == null || nomeCompleto.isBlank()) {
            return "";
        }
        String nome = nomeCompleto.trim().replaceAll("\\s+", " ");
        int virgula = nome.indexOf(',');
        if (virgula >= 0) {
            return nome.substring(0, virgula).trim();
        }
        String[] partes = nome.split(" ");
        return partes[partes.length - 1];
    }
}
