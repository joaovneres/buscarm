package com.buscarm.registro.adapter.in.web.dto;

import com.buscarm.registro.domain.Registro;

/** Conversão entre o modelo de domínio e os DTOs da web. */
public final class RegistroWebMapper {

    private RegistroWebMapper() {
    }

    public static RegistroResponse paraResponse(Registro r) {
        return new RegistroResponse(
                r.getId(),
                r.getRm(),
                r.getNomeCompleto(),
                r.getSobrenome(),
                r.getAnoRegistro(),
                r.isAtivo(),
                r.getCriadoPor(),
                r.getAtualizadoPor(),
                r.getCriadoEm()
        );
    }
}
