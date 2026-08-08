package com.buscarm.importacao.adapter.in.web.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;

/** Corpo da confirmação: a importação e as linhas aprovadas para gravação. */
public record ConfirmarRequest(
        @NotNull(message = "importacaoId é obrigatório") Long importacaoId,
        List<Integer> linhasAprovadas
) {
}
