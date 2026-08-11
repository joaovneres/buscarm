package com.buscarm.usuario.adapter.out.persistence;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

import com.buscarm.usuario.domain.Papel;

@Entity
@Table(name = "usuario")
public class UsuarioJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "senha_hash", nullable = false)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Papel papel;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "senha_provisoria", nullable = false)
    private boolean senhaProvisoria = false;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    protected UsuarioJpaEntity() {
    }

    public UsuarioJpaEntity(String nome, String username, String senhaHash, Papel papel,
                            boolean ativo, boolean senhaProvisoria) {
        this.nome = nome;
        this.username = username;
        this.senhaHash = senhaHash;
        this.papel = papel;
        this.ativo = ativo;
        this.senhaProvisoria = senhaProvisoria;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getUsername() {
        return username;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public Papel getPapel() {
        return papel;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public boolean isSenhaProvisoria() {
        return senhaProvisoria;
    }

    public void setSenhaProvisoria(boolean senhaProvisoria) {
        this.senhaProvisoria = senhaProvisoria;
    }
}
