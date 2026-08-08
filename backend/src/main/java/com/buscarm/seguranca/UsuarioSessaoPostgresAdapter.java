package com.buscarm.seguranca;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.buscarm.registro.application.port.out.UsuarioSessaoPort;

/**
 * Adaptador de saída para {@link UsuarioSessaoPort}: define a GUC 'app.current_username' na sessão
 * do Postgres, local à transação (set_config(..., true)). O trigger de auditoria (V2) lê essa GUC.
 */
@Component
@Profile("!test")
public class UsuarioSessaoPostgresAdapter implements UsuarioSessaoPort {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void definirUsuarioSessao(String usuario) {
        em.createNativeQuery("SELECT set_config('app.current_username', :usuario, true)")
                .setParameter("usuario", usuario)
                .getSingleResult();
    }
}
