package com.buscarm.seguranca;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.buscarm.usuario.application.port.out.CodificadorSenhaPort;

/** Adaptador de codificação de senha com BCrypt. */
@Component
public class BCryptCodificadorSenhaAdapter implements CodificadorSenhaPort {

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String codificar(String senha) {
        return encoder.encode(senha);
    }

    @Override
    public boolean confere(String senha, String hash) {
        return encoder.matches(senha, hash);
    }
}
