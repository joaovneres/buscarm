package com.buscarm.registro;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.buscarm.registro.adapter.in.web.dto.RegistroRequest;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser(username = "secretaria", roles = {"SECRETARIA"})
class RegistroIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void criaEBuscaPorNomeSemAcento() throws Exception {
        var req = new RegistroRequest(203, "Sousa, João Victor Cosme Neres de", null);

        mvc.perform(post("/api/registros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rm").value(203))
                .andExpect(jsonPath("$.sobrenome").value("Sousa"))
                .andExpect(jsonPath("$.status").value("VALIDADO"))
                .andExpect(jsonPath("$.criadoPor").value("secretaria"))
                .andExpect(jsonPath("$.atualizadoPor").value("secretaria"));

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

    @Test
    void alteraNomeEAnoDoRegistro() throws Exception {
        long id = criar(new RegistroRequest(700, "Silva, Ana", null));

        var alteracao = new RegistroRequest(700, "Silva, Ana Paula", 2019);
        mvc.perform(put("/api/registros/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(alteracao)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeCompleto").value("Silva, Ana Paula"))
                .andExpect(jsonPath("$.anoRegistro").value(2019))
                .andExpect(jsonPath("$.atualizadoPor").value("secretaria"));

        // a busca por nome reflete a alteração (chave de busca re-derivada)
        mvc.perform(get("/api/registros").param("nome", "ana paula"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.conteudo[0].rm").value(700));
    }

    @Test
    void alterarParaRmJaUsadoRetorna409() throws Exception {
        criar(new RegistroRequest(710, "Souza, Bruno", null));
        long id = criar(new RegistroRequest(711, "Souza, Bruna", null));

        var alteracao = new RegistroRequest(710, "Souza, Bruna", null);
        mvc.perform(put("/api/registros/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(alteracao)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("REGRA_NEGOCIO"));
    }

    @Test
    void alterarRegistroInexistenteRetorna404() throws Exception {
        var alteracao = new RegistroRequest(720, "Lima, Carlos", null);
        mvc.perform(put("/api/registros/{id}", 999999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(alteracao)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NAO_ENCONTRADO"));
    }

    @Test
    void desativaRegistroESaiDaBusca() throws Exception {
        long id = criar(new RegistroRequest(730, "Alves, Diego", null));

        mvc.perform(delete("/api/registros/{id}", id))
                .andExpect(status().isNoContent());

        // desativado não aparece mais na busca por RM (só ativos)
        mvc.perform(get("/api/registros").param("rm", "730"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    void validarMarcaComoValidadoERetornaOk() throws Exception {
        long id = criar(new RegistroRequest(740, "Prado, Igor", null));

        mvc.perform(patch("/api/registros/{id}/validar", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("VALIDADO"));
    }

    @Test
    @WithMockUser(username = "leitor", roles = {"VISUALIZADOR"})
    void visualizadorPodeBuscarMasNaoPodeEscrever() throws Exception {
        // leitura permitida
        mvc.perform(get("/api/registros"))
                .andExpect(status().isOk());

        // criação bloqueada (403)
        var req = new RegistroRequest(760, "Teles, Ura", null);
        mvc.perform(post("/api/registros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    private long criar(RegistroRequest req) throws Exception {
        String corpo = mvc.perform(post("/api/registros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(corpo);
        return json.get("id").asLong();
    }
}
