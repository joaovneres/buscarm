package com.buscarm.registro;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.buscarm.registro.dto.RegistroRequest;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RegistroIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private RegistroRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void limpar() {
        repository.deleteAll();
    }

    @Test
    void criaEBuscaPorNomeSemAcento() throws Exception {
        var req = new RegistroRequest(203, "Sousa, João Victor Cosme Neres de", null);

        mvc.perform(post("/api/registros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rm").value(203))
                .andExpect(jsonPath("$.sobrenome").value("Sousa"))
                .andExpect(jsonPath("$.criadoPor").value("sistema"))
                .andExpect(jsonPath("$.atualizadoPor").value("sistema"));

        mvc.perform(get("/api/registros").param("nome", "joao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.conteudo[0].rm").value(203));
    }

    @Test
    void buscaPorRm() throws Exception {
        var req = new RegistroRequest(503, "Souza, Lara", 2020);

        mvc.perform(post("/api/registros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/registros").param("rm", "503"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.conteudo[0].nomeCompleto").value("Souza, Lara"));
    }

    @Test
    void rmDuplicadoRetorna409() throws Exception {
        var req = new RegistroRequest(390, "Souza, Davi", null);
        String body = objectMapper.writeValueAsString(req);

        mvc.perform(post("/api/registros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/registros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("REGRA_NEGOCIO"));
    }

    @Test
    void dadosInvalidosRetorna400() throws Exception {
        mvc.perform(post("/api/registros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nomeCompleto\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDACAO"));
    }
}
