package com.buscarm.usuario.application.port.out;

import com.buscarm.usuario.domain.Usuario;

/** Porta de saída: geração do token de autenticação (JWT na infra). */
public interface GeradorTokenPort {

    String gerar(Usuario usuario);
}
