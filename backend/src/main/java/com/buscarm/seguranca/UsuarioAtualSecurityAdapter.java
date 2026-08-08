package com.buscarm.seguranca;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.buscarm.registro.application.port.out.UsuarioAtualPort;

/**
 * Adaptador de saída para {@link UsuarioAtualPort}. Hoje, sem autenticação (Épico 3), retorna
 * "sistema"; quando o JWT entrar, passa a retornar o usuário autenticado (ADR-0002).
 */
@Component
public class UsuarioAtualSecurityAdapter implements UsuarioAtualPort {

    public static final String USUARIO_SISTEMA = "sistema";

    @Override
    public String getUsuarioAtual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return auth.getName();
        }
        return USUARIO_SISTEMA;
    }
}
