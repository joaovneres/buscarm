package com.buscarm.importacao.domain;

/**
 * Registro cru extraído de uma fonte de importação (planilha hoje, OCR depois). É o DTO comum do
 * pipeline (ADR-0003): tanto Apache POI quanto o OCR produzem isto, e a validação é a mesma.
 */
public record RegistroExtraido(int linhaOrigem, Integer rm, String nomeCompleto) {
}
