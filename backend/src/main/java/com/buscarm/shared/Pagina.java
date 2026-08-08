package com.buscarm.shared;

import java.util.List;
import java.util.function.Function;

/**
 * Página de resultados independente de framework (não vaza tipos do Spring Data para o domínio
 * ou aplicação). Serve tanto às portas quanto à borda web.
 */
public record Pagina<T>(
        List<T> conteudo,
        int pagina,
        int tamanho,
        long total,
        int totalPaginas
) {
    public <R> Pagina<R> map(Function<T, R> mapper) {
        return new Pagina<>(
                conteudo.stream().map(mapper).toList(),
                pagina,
                tamanho,
                total,
                totalPaginas
        );
    }
}
