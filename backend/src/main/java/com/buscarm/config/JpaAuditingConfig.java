package com.buscarm.config;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import com.buscarm.shared.UsuarioAtualPort;

/** Liga a auditoria de autoria (@CreatedBy/@LastModifiedBy) ao usuário da sessão (porta). */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditingConfig {

    @Bean
    AuditorAware<String> auditorAware(UsuarioAtualPort usuarioAtual) {
        return () -> Optional.of(usuarioAtual.getUsuarioAtual());
    }
}
