package com.buscarm.seguranca;

import java.util.Arrays;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Segurança do BuscaRM: JWT stateless (ADR-0002). Login é público; o restante da API exige
 * autenticação; a auditoria exige papel ADMIN.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${buscarm.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, JwtService jwtService) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
                                "/actuator/health").permitAll()
                        // Admin: auditoria e gestão de usuários.
                        .requestMatchers("/api/auditoria/**").hasRole("ADMIN")
                        .requestMatchers("/api/usuarios/**").hasRole("ADMIN")
                        // Escrita de registros: ADMIN ou SECRETARIA (VISUALIZADOR não escreve).
                        .requestMatchers(HttpMethod.POST, "/api/registros/**").hasAnyRole("ADMIN", "SECRETARIA")
                        .requestMatchers(HttpMethod.PUT, "/api/registros/**").hasAnyRole("ADMIN", "SECRETARIA")
                        .requestMatchers(HttpMethod.PATCH, "/api/registros/**").hasAnyRole("ADMIN", "SECRETARIA")
                        .requestMatchers(HttpMethod.DELETE, "/api/registros/**").hasAnyRole("ADMIN", "SECRETARIA")
                        // Importação em lote é escrita: ADMIN ou SECRETARIA.
                        .requestMatchers("/api/importacoes/**").hasAnyRole("ADMIN", "SECRETARIA")
                        // Leitura (GET registros, exportação) e demais: qualquer autenticado.
                        .anyRequest().authenticated())
                .exceptionHandling(e -> e.authenticationEntryPoint(
                        (req, res, ex) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED)))
                .addFilterBefore(new JwtAuthenticationFilter(jwtService),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
