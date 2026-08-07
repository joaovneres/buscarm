package com.buscarm.seguranca;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Implementação usada nos testes (H2), onde não há GUC nem o trigger do Postgres. A auditoria de
 * autoria continua sendo exercida pelo JPA Auditing (@CreatedBy/@LastModifiedBy).
 */
@Component
@Profile("test")
public class NoOpUsuarioSessaoBanco implements UsuarioSessaoBanco {

    @Override
    public void definir(String usuario) {
        // sem operação
    }
}
