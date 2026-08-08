package com.buscarm.registro.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** DTO de entrada da API para cadastro de um registro. */
public record RegistroRequest(
        @NotNull(message = "RM é obrigatório")
        @Positive(message = "RM deve ser positivo")
        Integer rm,

        @NotBlank(message = "Nome é obrigatório")
        String nomeCompleto,

        Integer anoRegistro
) {
}
