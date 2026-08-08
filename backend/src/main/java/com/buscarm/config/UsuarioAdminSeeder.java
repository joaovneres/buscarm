package com.buscarm.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.buscarm.usuario.application.port.out.CodificadorSenhaPort;
import com.buscarm.usuario.application.port.out.UsuarioRepositoryPort;
import com.buscarm.usuario.domain.Papel;
import com.buscarm.usuario.domain.Usuario;

/** Cria um usuário ADMIN inicial se o banco não tiver nenhum usuário (T3.3). */
@Component
public class UsuarioAdminSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(UsuarioAdminSeeder.class);

    private final UsuarioRepositoryPort repositorio;
    private final CodificadorSenhaPort codificador;
    private final String nome;
    private final String email;
    private final String senha;

    public UsuarioAdminSeeder(UsuarioRepositoryPort repositorio,
                              CodificadorSenhaPort codificador,
                              @Value("${buscarm.admin.nome:Administrador}") String nome,
                              @Value("${buscarm.admin.email:admin@buscarm.local}") String email,
                              @Value("${buscarm.admin.senha:admin12345}") String senha) {
        this.repositorio = repositorio;
        this.codificador = codificador;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (repositorio.existeAlgum()) {
            return;
        }
        repositorio.salvar(Usuario.novo(nome, email, codificador.codificar(senha), Papel.ADMIN));
        log.warn("Usuário ADMIN inicial criado ({}). ALTERE a senha padrão em produção!", email);
    }
}
