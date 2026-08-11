package com.buscarm.usuario.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** O próprio usuário troca a senha (informa a atual e a nova). */
public record TrocarSenhaRequest(
        @NotBlank(message = "Senha atual é obrigatória")
        String senhaAtual,

        @NotBlank(message = "Nova senha é obrigatória")
        @Size(min = 8, message = "Nova senha deve ter ao menos 8 caracteres")
        String novaSenha
) {
}
