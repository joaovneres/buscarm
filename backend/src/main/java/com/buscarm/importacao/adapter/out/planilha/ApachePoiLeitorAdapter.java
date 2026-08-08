package com.buscarm.importacao.adapter.out.planilha;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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

/** Adaptador Apache POI: lê planilhas .xlsx reconhecendo as colunas "Nome" e "RM". */
@Component
public class ApachePoiLeitorAdapter implements LeitorPlanilhaPort {

    private static final DataFormatter FORMATTER = new DataFormatter();

    @Override
    public List<RegistroExtraido> ler(byte[] conteudo) {
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
            if (colRm == -1 || colNome == -1) {
                throw new IllegalArgumentException(
                        "Não foi possível identificar as colunas 'Nome' e 'RM' na planilha");
            }

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
