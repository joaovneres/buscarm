package com.buscarm.usuario.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Usuário é obrigatório") String username,
        @NotBlank(message = "Senha é obrigatória") String senha
) {
}
