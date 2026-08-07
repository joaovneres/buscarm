package com.buscarm.seguranca;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Define a GUC 'app.current_username' na sessão do Postgres, local à transação atual
 * (set_config(..., true)). O trigger de auditoria (V2) lê essa GUC ao gravar.
 */
@Component
@Profile("!test")
public class PostgresUsuarioSessaoBanco implements UsuarioSessaoBanco {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void definir(String usuario) {
        em.createNativeQuery("SELECT set_config('app.current_username', :usuario, true)")
                .setParameter("usuario", usuario)
                .getSingleResult();
    }
}
