package com.buscarm.usuario.adapter.in.web.dto;

import com.buscarm.usuario.domain.Usuario;

/** DTO de saída de usuário (sem o hash da senha). */
public record UsuarioResponse(Long id, String nome, String username, String papel, boolean ativo) {

    public static UsuarioResponse de(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNome(), u.getUsername(), u.getPapel().name(), u.isAtivo());
    }
}
