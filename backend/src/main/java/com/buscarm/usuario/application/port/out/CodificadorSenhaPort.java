package com.buscarm.usuario.application.port.out;

/** Porta de saída: codificação/verificação de senha (BCrypt na infra). */
public interface CodificadorSenhaPort {

    String codificar(String senha);

    boolean confere(String senha, String hash);
}
