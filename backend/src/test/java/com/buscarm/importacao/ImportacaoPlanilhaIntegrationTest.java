package com.buscarm.importacao;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayOutputStream;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ImportacaoPlanilhaIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void validaEConfirmaImportandoApenasOsAprovados() throws Exception {
        byte[] planilha = planilha(new Object[][]{
                {"RM", "Nome"},
                {203, "Sousa, João Victor Cosme Neres de"},
                {390, "Souza, Davi"},
                {null, "Sem RM"}
        });

        MvcResult resultado = mvc.perform(multipart("/api/importacoes/planilha/validar")
                        .file(new MockMultipartFile("arquivo", "alunos.xlsx",
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", planilha)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("VALIDADA"))
                .andExpect(jsonPath("$.resumo.total").value(3))
                .andExpect(jsonPath("$.resumo.validos").value(2))
                .andExpect(jsonPath("$.resumo.invalidos").value(1))
                .andReturn();

        long importacaoId = objectMapper.readTree(resultado.getResponse().getContentAsString())
                .get("id").asLong();

        // aprova as duas linhas válidas (linhas 2 e 3 da planilha)
        String body = objectMapper.writeValueAsString(
                new ConfirmarBody(importacaoId, new int[]{2, 3}));

        mvc.perform(post("/api/importacoes/planilha/confirmar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMADA"))
                .andExpect(jsonPath("$.resumo.importados").value(2));

        // os registros passam a ser encontráveis pela busca
        mvc.perform(get("/api/registros").param("nome", "joao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.conteudo[0].rm").value(203));
    }

    @Test
    void detectaDuplicidadeEConflitoContraOCadastro() throws Exception {
        // cadastra 503 = "Souza, Lara"
        mvc.perform(post("/api/registros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rm\":503,\"nomeCompleto\":\"Souza, Lara\"}"))
                .andExpect(status().isCreated());

        byte[] planilha = planilha(new Object[][]{
                {"RM", "Nome"},
                {503, "Souza, Lara"},    // duplicado (mesmo nome)
                {503, "Souza, Laura"},   // conflito (mesmo RM, nome diferente) — já visto no lote
                {700, "Pereira, Ana"}    // válido
        });

        mvc.perform(multipart("/api/importacoes/planilha/validar")
                        .file(new MockMultipartFile("arquivo", "alunos.xlsx",
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", planilha)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumo.total").value(3))
                .andExpect(jsonPath("$.resumo.duplicados").value(1))
                .andExpect(jsonPath("$.resumo.conflitos").value(1))
                .andExpect(jsonPath("$.resumo.validos").value(1));
    }

    @Test
    void arquivoSemColunasRetorna400() throws Exception {
        byte[] planilha = planilha(new Object[][]{
                {"Coluna A", "Coluna B"},
                {"x", "y"}
        });

        mvc.perform(multipart("/api/importacoes/planilha/validar")
                        .file(new MockMultipartFile("arquivo", "ruim.xlsx",
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", planilha)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ARQUIVO_INVALIDO"));
    }

    private static byte[] planilha(Object[][] linhas) throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("registros");
            for (int r = 0; r < linhas.length; r++) {
                Row row = sheet.createRow(r);
                for (int c = 0; c < linhas[r].length; c++) {
                    Object valor = linhas[r][c];
                    if (valor == null) {
                        continue;
                    }
                    if (valor instanceof Integer numero) {
                        row.createCell(c).setCellValue(numero);
                    } else {
                        row.createCell(c).setCellValue(valor.toString());
                    }
                }
            }
            wb.write(out);
            return out.toByteArray();
        }
    }

    private record ConfirmarBody(long importacaoId, int[] linhasAprovadas) {
    }
}
