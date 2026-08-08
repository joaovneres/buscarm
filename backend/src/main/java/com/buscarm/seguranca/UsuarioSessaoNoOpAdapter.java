package com.buscarm.seguranca;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.buscarm.registro.application.port.out.UsuarioSessaoPort;

/**
 * Adaptador usado nos testes (H2), onde não há GUC nem o trigger do Postgres. A auditoria de
 * autoria continua sendo exercida pelo JPA Auditing.
 */
@Component
@Profile("test")
public class UsuarioSessaoNoOpAdapter implements UsuarioSessaoPort {

    @Override
    public void definirUsuarioSessao(String usuario) {
        // sem operação
    }
}
