package com.buscarm.comum;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import com.buscarm.seguranca.UsuarioAtualProvider;

/** Habilita a auditoria de autoria (@CreatedBy/@LastModifiedBy) pelo usuário da sessão. */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditingConfig {

    @Bean
    AuditorAware<String> auditorAware(UsuarioAtualProvider provider) {
        return () -> Optional.of(provider.getUsuario());
    }
}
