package com.buscarm.registro.domain;

import java.time.Instant;

/**
 * Modelo de domínio do registro de matrícula (índice Nome -> RM). Imutável e sem dependência de
 * framework/infra. As regras de derivação (sobrenome, chave de busca) vivem aqui.
 */
public final class Registro {

    private final Long id;
    private final int rm;
    private final String nomeCompleto;
    private final String sobrenome;
    private final String nomeBusca;
    private final Integer anoRegistro;
    private final boolean ativo;
    private final String criadoPor;
    private final String atualizadoPor;
    private final Instant criadoEm;

    private Registro(Long id, int rm, String nomeCompleto, String sobrenome, String nomeBusca,
                     Integer anoRegistro, boolean ativo, String criadoPor, String atualizadoPor,
                     Instant criadoEm) {
        this.id = id;
        this.rm = rm;
        this.nomeCompleto = nomeCompleto;
        this.sobrenome = sobrenome;
        this.nomeBusca = nomeBusca;
        this.anoRegistro = anoRegistro;
        this.ativo = ativo;
        this.criadoPor = criadoPor;
        this.atualizadoPor = atualizadoPor;
        this.criadoEm = criadoEm;
    }

    /** Cria um novo registro ainda não persistido; deriva sobrenome e chave de busca. */
    public static Registro novo(int rm, String nomeCompleto, Integer anoRegistro) {
        String nome = nomeCompleto == null ? "" : nomeCompleto.trim();
        return new Registro(
                null,
                rm,
                nome,
                NomeNormalizer.extrairSobrenome(nome),
                NomeNormalizer.normalizar(nome),
                anoRegistro,
                true,
                null,
                null,
                null
        );
    }

    /** Reconstitui um registro já existente (a partir da persistência). */
    public static Registro reconstituir(Long id, int rm, String nomeCompleto, String sobrenome,
                                        String nomeBusca, Integer anoRegistro, boolean ativo,
                                        String criadoPor, String atualizadoPor, Instant criadoEm) {
        return new Registro(id, rm, nomeCompleto, sobrenome, nomeBusca, anoRegistro, ativo,
                criadoPor, atualizadoPor, criadoEm);
    }

    public Long getId() {
        return id;
    }

    public int getRm() {
        return rm;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public String getSobrenome() {
        return sobrenome;
    }

    public String getNomeBusca() {
        return nomeBusca;
    }

    public Integer getAnoRegistro() {
        return anoRegistro;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public String getCriadoPor() {
        return criadoPor;
    }

    public String getAtualizadoPor() {
        return atualizadoPor;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }
}
