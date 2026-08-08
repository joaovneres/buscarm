package com.buscarm.registro.adapter.out.exportacao;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import com.buscarm.registro.application.port.out.GeradorArquivoRegistrosPort;
import com.buscarm.registro.domain.FormatoExportacao;
import com.buscarm.registro.domain.Registro;
import com.buscarm.shared.ArquivoExportado;

/** Gera a exportação dos registros em .xlsx (Apache POI) ou .csv. */
@Component
public class GeradorArquivoRegistrosAdapter implements GeradorArquivoRegistrosPort {

    private static final String XLSX_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final String[] CABECALHO = {"RM", "Nome", "Ano"};

    @Override
    public ArquivoExportado gerar(List<Registro> registros, FormatoExportacao formato) {
        return formato == FormatoExportacao.CSV ? gerarCsv(registros) : gerarXlsx(registros);
    }

    private ArquivoExportado gerarXlsx(List<Registro> registros) {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("registros");
            Row cabecalho = sheet.createRow(0);
            for (int c = 0; c < CABECALHO.length; c++) {
                cabecalho.createCell(c).setCellValue(CABECALHO[c]);
            }
            int r = 1;
            for (Registro reg : registros) {
                Row linha = sheet.createRow(r++);
                linha.createCell(0).setCellValue(reg.getRm());
                linha.createCell(1).setCellValue(reg.getNomeCompleto());
                if (reg.getAnoRegistro() != null) {
                    linha.createCell(2).setCellValue(reg.getAnoRegistro());
                }
            }
            wb.write(out);
            return new ArquivoExportado("registros.xlsx", XLSX_TYPE, out.toByteArray());
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao gerar a planilha de exportação", e);
        }
    }

    private ArquivoExportado gerarCsv(List<Registro> registros) {
        StringBuilder sb = new StringBuilder("﻿"); // BOM UTF-8 para o Excel abrir com acentos
        sb.append(String.join(";", CABECALHO)).append("\r\n");
        for (Registro reg : registros) {
            sb.append(reg.getRm()).append(';')
                    .append(escapar(reg.getNomeCompleto())).append(';')
                    .append(reg.getAnoRegistro() != null ? reg.getAnoRegistro() : "")
                    .append("\r\n");
        }
        byte[] conteudo = sb.toString().getBytes(StandardCharsets.UTF_8);
        return new ArquivoExportado("registros.csv", "text/csv; charset=UTF-8", conteudo);
    }

    private static String escapar(String valor) {
        if (valor == null) {
            return "";
        }
        if (valor.contains(";") || valor.contains("\"") || valor.contains("\n") || valor.contains("\r")) {
            return '"' + valor.replace("\"", "\"\"") + '"';
        }
        return valor;
    }
}
