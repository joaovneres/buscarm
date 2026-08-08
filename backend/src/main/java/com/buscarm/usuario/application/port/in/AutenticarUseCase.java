package com.buscarm.usuario.application.port.in;

/** Porta de entrada: autentica por e-mail/senha e devolve um token JWT. */
public interface AutenticarUseCase {

    ResultadoLogin autenticar(String email, String senha);

    record ResultadoLogin(String token, String nome, String email, String papel) {
    }
}
