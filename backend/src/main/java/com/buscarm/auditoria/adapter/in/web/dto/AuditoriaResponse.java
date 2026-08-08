package com.buscarm.auditoria.adapter.in.web.dto;

import java.time.Instant;

public record AuditoriaResponse(
        Long id,
        String usuario,
        String acao,
        String entidade,
        Long entidadeId,
        String detalhe,
        Instant criadoEm
) {
}
