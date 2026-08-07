package com.buscarm.registro.dto;

import java.time.Instant;

/** Dados de saída de um registro. */
public record RegistroResponse(
        Long id,
        Integer rm,
        String nomeCompleto,
        String sobrenome,
        Integer anoRegistro,
        boolean ativo,
        String criadoPor,
        String atualizadoPor,
        Instant criadoEm
) {
}
