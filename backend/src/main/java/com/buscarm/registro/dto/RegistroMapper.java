package com.buscarm.registro.dto;

import com.buscarm.registro.Registro;

/** Conversão entre entidade e DTO de saída. */
public final class RegistroMapper {

    private RegistroMapper() {
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
