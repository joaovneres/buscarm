package com.buscarm.seguranca;

/**
 * Propaga o usuário da sessão da aplicação para a sessão do banco, de modo que o trigger de
 * auditoria (ver migration V2) grave/valide criado_por e atualizado_por.
 */
public interface UsuarioSessaoBanco {

    /** Deve ser chamado no início de cada transação de escrita, antes de persistir. */
    void definir(String usuario);
}
