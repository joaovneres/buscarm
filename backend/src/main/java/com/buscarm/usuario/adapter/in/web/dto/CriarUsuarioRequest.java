package com.buscarm.usuario.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.buscarm.usuario.domain.Papel;

/** DTO de entrada para criação de usuário (ADMIN). Login por username definido pelo admin. */
public record CriarUsuarioRequest(
        @NotBlank(message = "Nome é obrigatório")
        String nome,

        @NotBlank(message = "Usuário é obrigatório")
        @Size(min = 3, max = 50, message = "Usuário deve ter de 3 a 50 caracteres")
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$",
                message = "Usuário aceita apenas letras, números, ponto, hífen e sublinhado")
        String username,

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 8, message = "Senha deve ter ao menos 8 caracteres")
        String senha,

        @NotNull(message = "Papel é obrigatório")
        Papel papel
) {
}
