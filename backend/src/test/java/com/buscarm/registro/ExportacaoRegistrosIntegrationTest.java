package com.buscarm.registro;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ExportacaoRegistrosIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void exportaCsvComOsRegistros() throws Exception {
        cadastrar(503, "Souza, Lara");

        MvcResult resultado = mvc.perform(get("/api/registros/exportar").param("formato", "csv"))
                .andExpect(status().isOk())
                .andReturn();

        String csv = resultado.getResponse().getContentAsString();
        assertThat(resultado.getResponse().getContentType()).contains("text/csv");
        assertThat(csv).contains("RM;Nome;Ano");
        assertThat(csv).contains("503");
        assertThat(csv).contains("Souza, Lara");
    }

    @Test
    void exportaXlsxComOsRegistros() throws Exception {
        cadastrar(203, "Sousa, João Victor");

        MvcResult resultado = mvc.perform(get("/api/registros/exportar").param("formato", "xlsx"))
                .andExpect(status().isOk())
                .andReturn();

        byte[] conteudo = resultado.getResponse().getContentAsByteArray();
        assertThat(resultado.getResponse().getContentType()).contains("spreadsheetml");
        assertThat(conteudo).isNotEmpty();

        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(conteudo))) {
            Sheet sheet = wb.getSheetAt(0);
            Row primeira = sheet.getRow(1);
            assertThat((int) primeira.getCell(0).getNumericCellValue()).isEqualTo(203);
            assertThat(primeira.getCell(1).getStringCellValue()).isEqualTo("Sousa, João Victor");
        }
    }

    private void cadastrar(int rm, String nome) throws Exception {
        mvc.perform(post("/api/registros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rm\":" + rm + ",\"nomeCompleto\":\"" + nome + "\"}"))
                .andExpect(status().isCreated());
    }
}
