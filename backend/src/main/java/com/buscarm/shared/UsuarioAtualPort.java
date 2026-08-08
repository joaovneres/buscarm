package com.buscarm.shared;

/** Porta transversal: identidade do usuário da sessão atual (fornecida pela segurança). */
public interface UsuarioAtualPort {

    String getUsuarioAtual();
}
