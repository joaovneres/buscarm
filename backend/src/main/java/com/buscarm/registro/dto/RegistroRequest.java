package com.buscarm.registro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Dados de entrada para cadastro/alteração de um registro. */
public record RegistroRequest(
        @NotNull(message = "RM é obrigatório")
        @Positive(message = "RM deve ser positivo")
        Integer rm,

        @NotBlank(message = "Nome é obrigatório")
        String nomeCompleto,

        Integer anoRegistro
) {
}
