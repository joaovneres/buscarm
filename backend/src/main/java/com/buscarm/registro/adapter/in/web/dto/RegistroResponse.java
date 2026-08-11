package com.buscarm.registro.adapter.in.web.dto;

import java.time.Instant;

/** DTO de saída da API. */
public record RegistroResponse(
        Long id,
        Integer rm,
        String nomeCompleto,
        String sobrenome,
        Integer anoRegistro,
        boolean ativo,
        String status,
        String criadoPor,
        String atualizadoPor,
        Instant criadoEm,
        Instant atualizadoEm
) {
}
