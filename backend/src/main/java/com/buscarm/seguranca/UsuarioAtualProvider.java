package com.buscarm.seguranca;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Usuário da sessão atual. Hoje, sem autenticação (Épico 3), retorna "sistema"; quando o
 * JWT entrar, passa a retornar o usuário autenticado (ADR-0002). É a fonte única de identidade
 * para a auditoria de autoria (JPA Auditing) e para a GUC do banco.
 */
@Component
public class UsuarioAtualProvider {

    public static final String USUARIO_SISTEMA = "sistema";

    public String getUsuario() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return auth.getName();
        }
        return USUARIO_SISTEMA;
    }
}
