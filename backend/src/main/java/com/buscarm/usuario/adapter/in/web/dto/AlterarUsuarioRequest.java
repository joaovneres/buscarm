package com.buscarm.usuario.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.buscarm.usuario.domain.Papel;

/** DTO de entrada para alteração de nome e papel de um usuário (ADMIN). */
public record AlterarUsuarioRequest(
        @NotBlank(message = "Nome é obrigatório")
        String nome,

        @NotNull(message = "Papel é obrigatório")
        Papel papel
) {
}
