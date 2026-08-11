package com.buscarm.usuario.domain;

/** Usuário do sistema. Login por <b>username</b> (definido pelo admin). Imutável; sem framework. */
public final class Usuario {

    private final Long id;
    private final String nome;
    private final String username;
    private final String senhaHash;
    private final Papel papel;
    private final boolean ativo;
    private final boolean senhaProvisoria;

    private Usuario(Long id, String nome, String username, String senhaHash, Papel papel,
                    boolean ativo, boolean senhaProvisoria) {
        this.id = id;
        this.nome = nome;
        this.username = username;
        this.senhaHash = senhaHash;
        this.papel = papel;
        this.ativo = ativo;
        this.senhaProvisoria = senhaProvisoria;
    }

    public static Usuario novo(String nome, String username, String senhaHash, Papel papel) {
        return new Usuario(null, nome, username.trim().toLowerCase(), senhaHash, papel, true, false);
    }

    public static Usuario reconstituir(Long id, String nome, String username, String senhaHash,
                                       Papel papel, boolean ativo, boolean senhaProvisoria) {
        return new Usuario(id, nome, username, senhaHash, papel, ativo, senhaProvisoria);
    }

    /** Cópia com nova senha; {@code provisoria=true} quando definida pelo admin (reset). */
    public Usuario comSenha(String novoHash, boolean provisoria) {
        return new Usuario(id, nome, username, novoHash, papel, ativo, provisoria);
    }

    /** Cópia com nome e papel alterados (username e credenciais preservados). */
    public Usuario comDados(String novoNome, Papel novoPapel) {
        return new Usuario(id, novoNome, username, senhaHash, novoPapel, ativo, senhaProvisoria);
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

    public String getSenhaHash() {
        return senhaHash;
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
}
