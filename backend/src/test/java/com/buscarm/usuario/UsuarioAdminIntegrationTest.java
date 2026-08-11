package com.buscarm.usuario;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.buscarm.usuario.application.port.out.CodificadorSenhaPort;
import com.buscarm.usuario.application.port.out.UsuarioRepositoryPort;
import com.buscarm.usuario.domain.Papel;
import com.buscarm.usuario.domain.Usuario;

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
class UsuarioAdminIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepositoryPort usuarios;

    @Autowired
    private CodificadorSenhaPort senhas;

    private static final String NOVO = """
            {"nome":"Ana Leitora","username":"ana.leitora","senha":"segredo123","papel":"VISUALIZADOR"}""";

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void adminCriaEListaUsuarios() throws Exception {
        mvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON).content(NOVO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("ana.leitora"))
                .andExpect(jsonPath("$.papel").value("VISUALIZADOR"));

        mvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.username=='ana.leitora')]").exists());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void usernameDuplicadoRetorna409() throws Exception {
        mvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON).content(NOVO))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON).content(NOVO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("REGRA_NEGOCIO"));
    }

    @Test
    @WithMockUser(username = "sec", roles = {"SECRETARIA"})
    void naoAdminRecebe403() throws Exception {
        mvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON).content(NOVO))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void dadosInvalidosRetorna400() throws Exception {
        mvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"X\",\"username\":\"ab\",\"senha\":\"123\",\"papel\":\"ADMIN\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDACAO"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void adminResetaSenhaMarcandoComoProvisoria() throws Exception {
        String corpo = mvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON).content(NOVO))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(corpo).get("id").asLong();

        mvc.perform(patch("/api/usuarios/{id}/senha", id)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"senha\":\"temp12345\"}"))
                .andExpect(status().isNoContent());

        // login com a nova senha reflete provisória=true
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"ana.leitora\",\"senha\":\"temp12345\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.senhaProvisoria").value(true));
    }

    @Test
    @WithMockUser(username = "ana.leitora", roles = {"VISUALIZADOR"})
    void usuarioTrocaPropriaSenhaELimpaProvisoria() throws Exception {
        // simula um usuário com senha provisória (após reset)
        usuarios.salvar(Usuario.novo("Ana Leitora", "ana.leitora", senhas.codificar("temp12345"),
                Papel.VISUALIZADOR).comSenha(senhas.codificar("temp12345"), true));

        mvc.perform(patch("/api/conta/senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"senhaAtual\":\"temp12345\",\"novaSenha\":\"minhaNova123\"}"))
                .andExpect(status().isNoContent());

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"ana.leitora\",\"senha\":\"minhaNova123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.senhaProvisoria").value(false));
    }

    @Test
    @WithMockUser(username = "ana.leitora", roles = {"VISUALIZADOR"})
    void trocaComSenhaAtualErradaRetorna401() throws Exception {
        usuarios.salvar(Usuario.novo("Ana Leitora", "ana.leitora", senhas.codificar("temp12345"),
                Papel.VISUALIZADOR));

        mvc.perform(patch("/api/conta/senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"senhaAtual\":\"errada\",\"novaSenha\":\"minhaNova123\"}"))
                .andExpect(status().isUnauthorized());
    }
}
