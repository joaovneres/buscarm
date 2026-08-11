package com.buscarm.usuario.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Admin define uma nova senha (provisória) para um usuário. */
public record ResetarSenhaRequest(
        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 8, message = "Senha deve ter ao menos 8 caracteres")
        String senha
) {
}
