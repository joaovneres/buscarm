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
    private final StatusValidacao status;
    private final String criadoPor;
    private final String atualizadoPor;
    private final Instant criadoEm;
    private final Instant atualizadoEm;

    private Registro(Long id, int rm, String nomeCompleto, String sobrenome, String nomeBusca,
                     Integer anoRegistro, boolean ativo, StatusValidacao status, String criadoPor,
                     String atualizadoPor, Instant criadoEm, Instant atualizadoEm) {
        this.id = id;
        this.rm = rm;
        this.nomeCompleto = nomeCompleto;
        this.sobrenome = sobrenome;
        this.nomeBusca = nomeBusca;
        this.anoRegistro = anoRegistro;
        this.ativo = ativo;
        this.status = status;
        this.criadoPor = criadoPor;
        this.atualizadoPor = atualizadoPor;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    /** Novo registro (cadastro manual): já nasce VALIDADO. */
    public static Registro novo(int rm, String nomeCompleto, Integer anoRegistro) {
        return novo(rm, nomeCompleto, anoRegistro, StatusValidacao.VALIDADO);
    }

    /** Novo registro com status explícito (importação em lote nasce PENDENTE). */
    public static Registro novo(int rm, String nomeCompleto, Integer anoRegistro, StatusValidacao status) {
        String nome = nomeCompleto == null ? "" : nomeCompleto.trim();
        return new Registro(
                null,
                rm,
                nome,
                NomeNormalizer.extrairSobrenome(nome),
                NomeNormalizer.normalizar(nome),
                anoRegistro,
                true,
                status == null ? StatusValidacao.VALIDADO : status,
                null,
                null,
                null,
                null
        );
    }

    /** Reconstitui um registro já existente (a partir da persistência). */
    public static Registro reconstituir(Long id, int rm, String nomeCompleto, String sobrenome,
                                        String nomeBusca, Integer anoRegistro, boolean ativo,
                                        StatusValidacao status, String criadoPor, String atualizadoPor,
                                        Instant criadoEm, Instant atualizadoEm) {
        return new Registro(id, rm, nomeCompleto, sobrenome, nomeBusca, anoRegistro, ativo, status,
                criadoPor, atualizadoPor, criadoEm, atualizadoEm);
    }

    /** Cópia com RM/nome/ano alterados; re-deriva sobrenome e chave de busca. Preserva status/autoria. */
    public Registro comDados(int rm, String nomeCompleto, Integer anoRegistro) {
        String nome = nomeCompleto == null ? "" : nomeCompleto.trim();
        return new Registro(id, rm, nome, NomeNormalizer.extrairSobrenome(nome),
                NomeNormalizer.normalizar(nome), anoRegistro, ativo, status, criadoPor, atualizadoPor,
                criadoEm, atualizadoEm);
    }

    /** Cópia inativa (exclusão lógica — o prontuário físico continua existindo). */
    public Registro desativado() {
        return new Registro(id, rm, nomeCompleto, sobrenome, nomeBusca, anoRegistro, false, status,
                criadoPor, atualizadoPor, criadoEm, atualizadoEm);
    }

    /** Cópia marcada como validada (conferência humana concluída). */
    public Registro validado() {
        return new Registro(id, rm, nomeCompleto, sobrenome, nomeBusca, anoRegistro, ativo,
                StatusValidacao.VALIDADO, criadoPor, atualizadoPor, criadoEm, atualizadoEm);
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

    public StatusValidacao getStatus() {
        return status;
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

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}
