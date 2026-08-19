package com.buscarm.importacao.adapter.out.planilha;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import com.buscarm.importacao.application.port.out.LeitorPlanilhaPort;
import com.buscarm.importacao.domain.RegistroExtraido;
import com.buscarm.registro.domain.NomeNormalizer;

/**
 * Adaptador de leitura de planilhas (ADR-0003): despacha por formato reconhecendo as colunas
 * "Nome" e "RM". Aceita <b>.xlsx</b> (Apache POI) e <b>.csv</b> (RFC&nbsp;4180) — ambos produzem
 * o mesmo {@link RegistroExtraido}, então a validação e a gravação seguem idênticas.
 */
@Component
public class LeitorPlanilhaAdapter implements LeitorPlanilhaPort {

    private static final DataFormatter FORMATTER = new DataFormatter();

    /** Assinatura ZIP ("PK\03\04") no início de todo arquivo .xlsx (OOXML é um zip). */
    private static final byte[] ASSINATURA_XLSX = {0x50, 0x4B, 0x03, 0x04};

    @Override
    public List<RegistroExtraido> ler(byte[] conteudo) {
        if (conteudo == null || conteudo.length == 0) {
            throw new IllegalArgumentException("Planilha vazia");
        }
        return ehXlsx(conteudo) ? lerXlsx(conteudo) : lerCsv(conteudo);
    }

    private static boolean ehXlsx(byte[] conteudo) {
        if (conteudo.length < ASSINATURA_XLSX.length) {
            return false;
        }
        for (int i = 0; i < ASSINATURA_XLSX.length; i++) {
            if (conteudo[i] != ASSINATURA_XLSX[i]) {
                return false;
            }
        }
        return true;
    }

    // --- XLSX (Apache POI) ---

    private List<RegistroExtraido> lerXlsx(byte[] conteudo) {
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(conteudo))) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new IllegalArgumentException("Planilha vazia");
            }
            Row cabecalho = sheet.getRow(sheet.getFirstRowNum());
            int colRm = -1;
            int colNome = -1;
            if (cabecalho != null) {
                for (Cell celula : cabecalho) {
                    String titulo = NomeNormalizer.normalizar(texto(celula));
                    if (colRm == -1 && titulo.contains("rm")) {
                        colRm = celula.getColumnIndex();
                    } else if (colNome == -1 && titulo.contains("nome")) {
                        colNome = celula.getColumnIndex();
                    }
                }
            }
            exigirColunas(colRm, colNome);

            List<RegistroExtraido> extraidos = new ArrayList<>();
            for (int r = cabecalho.getRowNum() + 1; r <= sheet.getLastRowNum(); r++) {
                Row linha = sheet.getRow(r);
                if (linha == null) {
                    continue;
                }
                String nome = texto(linha.getCell(colNome));
                Integer rm = inteiro(texto(linha.getCell(colRm)));
                if (rm == null && nome.isBlank()) {
                    continue; // linha totalmente vazia
                }
                extraidos.add(new RegistroExtraido(r + 1, rm, nome.isBlank() ? null : nome));
            }
            return extraidos;
        } catch (IOException e) {
            throw new IllegalArgumentException("Não foi possível ler a planilha: " + e.getMessage());
        }
    }

    // --- CSV (RFC 4180) ---

    private List<RegistroExtraido> lerCsv(byte[] conteudo) {
        String texto = new String(conteudo, StandardCharsets.UTF_8);
        if (!texto.isEmpty() && texto.charAt(0) == '﻿') {
            texto = texto.substring(1); // descarta BOM UTF-8 (U+FEFF)
        }
        char delimitador = detectarDelimitador(texto);
        List<List<String>> linhas = parseCsv(texto, delimitador);
        if (linhas.isEmpty()) {
            throw new IllegalArgumentException("Planilha vazia");
        }

        List<String> cabecalho = linhas.get(0);
        int colRm = -1;
        int colNome = -1;
        for (int c = 0; c < cabecalho.size(); c++) {
            String titulo = NomeNormalizer.normalizar(cabecalho.get(c));
            if (colRm == -1 && titulo.contains("rm")) {
                colRm = c;
            } else if (colNome == -1 && titulo.contains("nome")) {
                colNome = c;
            }
        }
        exigirColunas(colRm, colNome);

        List<RegistroExtraido> extraidos = new ArrayList<>();
        for (int r = 1; r < linhas.size(); r++) {
            List<String> campos = linhas.get(r);
            String nome = valor(campos, colNome);
            Integer rm = inteiro(valor(campos, colRm));
            if (rm == null && nome.isBlank()) {
                continue; // linha totalmente vazia
            }
            extraidos.add(new RegistroExtraido(r + 1, rm, nome.isBlank() ? null : nome));
        }
        return extraidos;
    }

    /**
     * Detecta o separador do CSV pela primeira linha não vazia. Planilhas brasileiras costumam
     * exportar com ";" (a vírgula é separador decimal); aceitamos também "," e tabulação.
     */
    private static char detectarDelimitador(String texto) {
        int fim = texto.indexOf('\n');
        String primeira = (fim == -1 ? texto : texto.substring(0, fim));
        int pontoVirgula = contar(primeira, ';');
        int virgula = contar(primeira, ',');
        int tab = contar(primeira, '\t');
        if (pontoVirgula >= virgula && pontoVirgula >= tab && pontoVirgula > 0) {
            return ';';
        }
        if (tab > virgula && tab > 0) {
            return '\t';
        }
        return ',';
    }

    private static int contar(String texto, char alvo) {
        int total = 0;
        for (int i = 0; i < texto.length(); i++) {
            if (texto.charAt(i) == alvo) {
                total++;
            }
        }
        return total;
    }

    /** Parser RFC 4180: aspas duplas delimitam campos, "" é aspa literal, delimitador/quebra dentro de aspas é dado. */
    private static List<List<String>> parseCsv(String texto, char delimitador) {
        List<List<String>> linhas = new ArrayList<>();
        List<String> linhaAtual = new ArrayList<>();
        StringBuilder campo = new StringBuilder();
        boolean dentroAspas = false;
        boolean linhaComConteudo = false;

        for (int i = 0; i < texto.length(); i++) {
            char ch = texto.charAt(i);
            if (dentroAspas) {
                if (ch == '"') {
                    if (i + 1 < texto.length() && texto.charAt(i + 1) == '"') {
                        campo.append('"');
                        i++;
                    } else {
                        dentroAspas = false;
                    }
                } else {
                    campo.append(ch);
                }
                continue;
            }
            if (ch == '"') {
                dentroAspas = true;
                linhaComConteudo = true;
            } else if (ch == delimitador) {
                linhaAtual.add(campo.toString().trim());
                campo.setLength(0);
                linhaComConteudo = true;
            } else if (ch == '\n' || ch == '\r') {
                if (ch == '\r' && i + 1 < texto.length() && texto.charAt(i + 1) == '\n') {
                    i++; // consome o \n do par \r\n
                }
                linhaAtual.add(campo.toString().trim());
                campo.setLength(0);
                if (linhaComConteudo) {
                    linhas.add(linhaAtual);
                }
                linhaAtual = new ArrayList<>();
                linhaComConteudo = false;
            } else {
                campo.append(ch);
                if (!Character.isWhitespace(ch)) {
                    linhaComConteudo = true;
                }
            }
        }
        // último campo/linha sem quebra final
        linhaAtual.add(campo.toString().trim());
        if (linhaComConteudo) {
            linhas.add(linhaAtual);
        }
        return linhas;
    }

    private static String valor(List<String> campos, int coluna) {
        return coluna >= 0 && coluna < campos.size() ? campos.get(coluna).trim() : "";
    }

    // --- comuns ---

    private static void exigirColunas(int colRm, int colNome) {
        if (colRm == -1 || colNome == -1) {
            throw new IllegalArgumentException(
                    "Não foi possível identificar as colunas 'Nome' e 'RM' na planilha");
        }
    }

    private static String texto(Cell celula) {
        return celula == null ? "" : FORMATTER.formatCellValue(celula).trim();
    }

    private static Integer inteiro(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(valor.trim());
        } catch (NumberFormatException e) {
            try {
                return (int) Double.parseDouble(valor.trim());
            } catch (NumberFormatException ex) {
                return null;
            }
        }
    }
}
