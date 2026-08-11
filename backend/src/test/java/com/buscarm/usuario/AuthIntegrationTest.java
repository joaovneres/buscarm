package com.buscarm.usuario;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.buscarm.usuario.application.port.out.CodificadorSenhaPort;
import com.buscarm.usuario.application.port.out.UsuarioRepositoryPort;
import com.buscarm.usuario.domain.Papel;
import com.buscarm.usuario.domain.Usuario;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsuarioRepositoryPort usuarios;

    @Autowired
    private CodificadorSenhaPort senhas;

    @BeforeEach
    void criarUsuario() {
        usuarios.salvar(Usuario.novo("Secretaria Teste", "secretaria",
                senhas.codificar("senha123"), Papel.SECRETARIA));
    }

    @Test
    void loginValidoRetornaToken() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"secretaria\",\"senha\":\"senha123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("secretaria"))
                .andExpect(jsonPath("$.papel").value("SECRETARIA"));
    }

    @Test
    void senhaErradaRetorna401() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"secretaria\",\"senha\":\"errada\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("CREDENCIAIS_INVALIDAS"));
    }

    @Test
    void acessoSemTokenRetorna401() throws Exception {
        mvc.perform(get("/api/registros"))
                .andExpect(status().isUnauthorized());
    }
}
