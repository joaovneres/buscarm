package com.buscarm.importacao;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * Exercita o pipeline de importação por imagem via o stub de OCR (que interpreta o conteúdo
 * enviado como o texto reconhecido). Valida processar -> conferência -> confirmar com correção.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser(username = "secretaria", roles = {"SECRETARIA"})
class ImportacaoImagemIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void processaConfereEConfirmaComCorrecao() throws Exception {
        // linha 3 sai sem RM (INVALIDO) — será corrigida na conferência
        byte[] foto = ("203 Sousa, João Victor\n"
                + "204 Silva, Maria\n"
                + "Pereira, Carlos Eduardo").getBytes(StandardCharsets.UTF_8);

        MvcResult resultado = mvc.perform(multipart("/api/importacoes/imagem/processar")
                        .file(new MockMultipartFile("arquivo", "pagina.jpg", "image/jpeg", foto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("IMAGEM"))
                .andExpect(jsonPath("$.resumo.total").value(3))
                .andExpect(jsonPath("$.resumo.validos").value(2))
                .andExpect(jsonPath("$.resumo.invalidos").value(1))
                .andReturn();

        long id = objectMapper.readTree(resultado.getResponse().getContentAsString()).get("id").asLong();

        // conferência: aprova as 3 linhas, corrigindo a linha 3 (adiciona o RM)
        String body = """
                {"importacaoId": %d, "itens": [
                    {"linha": 1, "rm": 203, "nomeCompleto": "Sousa, João Victor Cosme Neres de"},
                    {"linha": 2, "rm": 204, "nomeCompleto": "Silva, Maria Aparecida"},
                    {"linha": 3, "rm": 205, "nomeCompleto": "Pereira, Carlos Eduardo"}
                ]}""".formatted(id);

        mvc.perform(post("/api/importacoes/imagem/confirmar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMADA"))
                .andExpect(jsonPath("$.resumo.importados").value(3));

        // o registro corrigido (linha 3) passa a ser encontrável
        mvc.perform(get("/api/registros").param("rm", "205"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.conteudo[0].nomeCompleto").value("Pereira, Carlos Eduardo"));
    }
}
