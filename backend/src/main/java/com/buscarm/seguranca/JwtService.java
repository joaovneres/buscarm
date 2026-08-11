package com.buscarm.seguranca;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.buscarm.usuario.application.port.out.GeradorTokenPort;
import com.buscarm.usuario.domain.Usuario;

/** Gera e valida tokens JWT (HS256). Implementa a porta de geração (ADR-0002). */
@Component
public class JwtService implements GeradorTokenPort {

    private final SecretKey chave;
    private final long expiracaoMs;

    public JwtService(
            @Value("${buscarm.jwt.secret:troque-este-segredo-de-desenvolvimento-buscarm-32bytes+}") String secret,
            @Value("${buscarm.jwt.expiration-minutes:480}") long minutos) {
        this.chave = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiracaoMs = minutos * 60_000L;
    }

    @Override
    public String gerar(Usuario usuario) {
        Instant agora = Instant.now();
        return Jwts.builder()
                .subject(usuario.getUsername())
                .claim("papel", usuario.getPapel().name())
                .claim("nome", usuario.getNome())
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plusMillis(expiracaoMs)))
                .signWith(chave)
                .compact();
    }

    public Optional<Autenticado> validar(String token) {
        try {
            Claims c = Jwts.parser().verifyWith(chave).build()
                    .parseSignedClaims(token).getPayload();
            return Optional.of(new Autenticado(c.getSubject(), c.get("papel", String.class)));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public record Autenticado(String username, String papel) {
    }
}
