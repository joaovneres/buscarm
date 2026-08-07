package com.buscarm.comum;

import java.time.Instant;
import java.util.List;

/** Corpo padronizado de erro da API. */
public record ApiError(
        Instant timestamp,
        int status,
        String code,
        String message,
        String path,
        List<CampoErro> fieldErrors
) {
    public record CampoErro(String campo, String mensagem) {
    }
}
