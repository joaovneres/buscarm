package com.buscarm.registro.domain;

/**
 * Critérios opcionais de busca de registros (todos combináveis; nulos/vazios são ignorados).
 * Sempre restringe a registros ativos.
 */
public record FiltroRegistros(
        String nome,
        Integer rm,
        StatusValidacao status,
        Integer ano,
        String inicial
) {
    public static FiltroRegistros vazio() {
        return new FiltroRegistros(null, null, null, null, null);
    }

    public static FiltroRegistros porRm(int rm) {
        return new FiltroRegistros(null, rm, null, null, null);
    }
}
