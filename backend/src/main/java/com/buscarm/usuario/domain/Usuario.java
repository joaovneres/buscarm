package com.buscarm.usuario.domain;

/** Usuário do sistema (secretaria/admin). Imutável; sem dependência de framework. */
public final class Usuario {

    private final Long id;
    private final String nome;
    private final String email;
    private final String senhaHash;
    private final Papel papel;
    private final boolean ativo;

    private Usuario(Long id, String nome, String email, String senhaHash, Papel papel, boolean ativo) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senhaHash = senhaHash;
        this.papel = papel;
        this.ativo = ativo;
    }

    public static Usuario novo(String nome, String email, String senhaHash, Papel papel) {
        return new Usuario(null, nome, email.trim().toLowerCase(), senhaHash, papel, true);
    }

    public static Usuario reconstituir(Long id, String nome, String email, String senhaHash,
                                       Papel papel, boolean ativo) {
        return new Usuario(id, nome, email, senhaHash, papel, ativo);
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public Papel getPapel() {
        return papel;
    }

    public boolean isAtivo() {
        return ativo;
    }
}
