package com.buscarm.importacao.adapter.in.web.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;

/** Confirmação da importação por imagem: a importação e as linhas revisadas/aprovadas. */
public record ConfirmarImagemRequest(
        @NotNull(message = "importacaoId é obrigatório") Long importacaoId,
        List<ItemCorrigidoRequest> itens
) {
    public record ItemCorrigidoRequest(int linha, Integer rm, String nomeCompleto) {
    }
}
