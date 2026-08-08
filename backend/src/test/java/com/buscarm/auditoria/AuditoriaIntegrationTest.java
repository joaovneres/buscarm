package com.buscarm.auditoria;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser(username = "admin", roles = {"ADMIN"})
class AuditoriaIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void registraAcaoDeCriacaoDeRegistro() throws Exception {
        mvc.perform(post("/api/registros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rm\":777,\"nomeCompleto\":\"Teste, Auditoria\"}"))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/auditoria"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo[?(@.acao=='CRIAR')]").isNotEmpty())
                .andExpect(jsonPath("$.conteudo[?(@.entidade=='REGISTRO')]").isNotEmpty());
    }

    @Test
    @WithMockUser(username = "sec", roles = {"SECRETARIA"})
    void secretariaNaoAcessaAuditoria() throws Exception {
        mvc.perform(get("/api/auditoria"))
                .andExpect(status().isForbidden());
    }
}
