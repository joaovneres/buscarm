package com.buscarm.usuario.adapter.in.web.dto;

public record LoginResponse(String token, String nome, String email, String papel) {
}
