package com.buscarm.importacao.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converte o texto reconhecido pelo OCR em registros extraídos. Regra de domínio pura — usada
 * tanto pelo adaptador Tesseract quanto pelos testes. Reconhece o número do RM no fim da linha
 * (formato do livro: "Sobrenome, Nome ... 203") ou no início ("203 Sobrenome, Nome").
 */
public final class ExtratorLinhasOcr {

    private static final Pattern RM_NO_FIM = Pattern.compile("^(.*?)[\\s.\\-]*(\\d{1,6})\\s*$");
    private static final Pattern RM_NO_INICIO = Pattern.compile("^\\s*(\\d{1,6})[\\s.\\-]*(.*)$");
    private static final Pattern TEM_LETRA = Pattern.compile("\\p{L}");

    private ExtratorLinhasOcr() {
    }

    public static List<RegistroExtraido> extrair(String textoOcr) {
        List<RegistroExtraido> registros = new ArrayList<>();
        if (textoOcr == null) {
            return registros;
        }
        String[] linhas = textoOcr.split("\\r?\\n");
        for (int i = 0; i < linhas.length; i++) {
            String linha = linhas[i].trim();
            if (linha.isEmpty() || !TEM_LETRA.matcher(linha).find()) {
                continue; // ignora linhas vazias ou sem nome
            }
            registros.add(interpretar(i + 1, linha));
        }
        return registros;
    }

    private static RegistroExtraido interpretar(int linhaOrigem, String linha) {
        Matcher fim = RM_NO_FIM.matcher(linha);
        if (fim.matches() && TEM_LETRA.matcher(fim.group(1)).find()) {
            return new RegistroExtraido(linhaOrigem, parseRm(fim.group(2)), limpar(fim.group(1)));
        }
        Matcher inicio = RM_NO_INICIO.matcher(linha);
        if (inicio.matches() && TEM_LETRA.matcher(inicio.group(2)).find()) {
            return new RegistroExtraido(linhaOrigem, parseRm(inicio.group(1)), limpar(inicio.group(2)));
        }
        return new RegistroExtraido(linhaOrigem, null, limpar(linha));
    }

    private static Integer parseRm(String valor) {
        try {
            return Integer.valueOf(valor);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String limpar(String nome) {
        String limpo = nome.trim().replaceAll("\\s+", " ");
        return limpo.isBlank() ? null : limpo;
    }
}
