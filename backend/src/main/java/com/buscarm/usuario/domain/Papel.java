package com.buscarm.usuario.domain;

/**
 * Papéis de acesso.
 * <ul>
 *   <li>{@code ADMIN} — CRUD de alunos + gestão de usuários + auditoria.</li>
 *   <li>{@code SECRETARIA} — acesso completo ao CRUD de alunos (sem usuários/auditoria).</li>
 *   <li>{@code VISUALIZADOR} — somente leitura (consulta e exportação).</li>
 * </ul>
 */
public enum Papel {
    ADMIN,
    SECRETARIA,
    VISUALIZADOR
}
