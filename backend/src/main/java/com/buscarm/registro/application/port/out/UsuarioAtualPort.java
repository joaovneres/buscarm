package com.buscarm.registro.application.port.out;

/** Porta de saída: identidade do usuário da sessão atual (fornecida pela camada de segurança). */
public interface UsuarioAtualPort {

    String getUsuarioAtual();
}
