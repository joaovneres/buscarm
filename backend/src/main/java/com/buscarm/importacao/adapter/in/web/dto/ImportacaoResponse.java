package com.buscarm.importacao.adapter.in.web.dto;

import java.util.List;

/** DTO de saída de uma importação: cabeçalho, resumo e itens (para a pré-visualização/relatório). */
public record ImportacaoResponse(
        Long id,
        String tipo,
        String nomeArquivo,
        String status,
        Resumo resumo,
        List<Item> itens
) {
    public record Item(int linha, Integer rm, String nome, String situacao, String mensagem, Long registroId) {
    }

    public record Resumo(
            int total,
            long validos,
            long duplicados,
            long conflitos,
            long invalidos,
            long importados,
            long ignorados
    ) {
    }
}
