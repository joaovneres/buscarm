package com.buscarm.registro.application.port.out;

/**
 * Porta de saída: propaga o usuário da sessão para a sessão do banco, de modo que o trigger de
 * auditoria (migration V2) grave/valide criado_por e atualizado_por.
 */
public interface UsuarioSessaoPort {

    /** Chamado no início de cada transação de escrita, antes de persistir. */
    void definirUsuarioSessao(String usuario);
}
