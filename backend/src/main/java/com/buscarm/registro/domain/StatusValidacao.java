package com.buscarm.registro.domain;

/**
 * Situação de validação do registro (conferência humana).
 * <ul>
 *   <li>{@code VALIDADO} — dado conferido; cadastro manual nasce assim.</li>
 *   <li>{@code PENDENTE} — precisa de conferência; importação em lote (Excel/OCR) nasce assim.</li>
 * </ul>
 * É independente do {@code ativo} (exclusão lógica).
 */
public enum StatusValidacao {
    PENDENTE,
    VALIDADO
}
