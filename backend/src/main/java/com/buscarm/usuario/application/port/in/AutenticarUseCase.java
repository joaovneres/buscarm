package com.buscarm.usuario.application.port.in;

/** Porta de entrada: autentica por username/senha e devolve um token JWT. */
public interface AutenticarUseCase {

    ResultadoLogin autenticar(String username, String senha);

    record ResultadoLogin(String token, String nome, String username, String papel,
                          boolean senhaProvisoria) {
    }
}
