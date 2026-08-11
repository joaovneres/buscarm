package com.buscarm.seguranca;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/** Lê o token Bearer, valida e popula o SecurityContext com o papel do usuário. */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIXO = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(PREFIXO)) {
            jwtService.validar(header.substring(PREFIXO.length())).ifPresent(a -> {
                var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + a.papel()));
                var auth = new UsernamePasswordAuthenticationToken(a.username(), null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            });
        }
        chain.doFilter(request, response);
    }
}
