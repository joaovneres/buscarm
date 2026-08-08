package com.buscarm.auditoria.domain;

import java.time.Instant;

/** Um evento de auditoria: quem fez, qual ação, sobre qual entidade. */
public class RegistroAuditoria {

    private final Long id;
    private final String usuario;
    private final String acao;
    private final String entidade;
    private final Long entidadeId;
    private final String detalhe;
    private final Instant criadoEm;

    public RegistroAuditoria(Long id, String usuario, String acao, String entidade,
                             Long entidadeId, String detalhe, Instant criadoEm) {
        this.id = id;
        this.usuario = usuario;
        this.acao = acao;
        this.entidade = entidade;
        this.entidadeId = entidadeId;
        this.detalhe = detalhe;
        this.criadoEm = criadoEm;
    }

    public static RegistroAuditoria novo(String usuario, String acao, String entidade,
                                         Long entidadeId, String detalhe) {
        return new RegistroAuditoria(null, usuario, acao, entidade, entidadeId, detalhe, null);
    }

    public Long getId() {
        return id;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getAcao() {
        return acao;
    }

    public String getEntidade() {
        return entidade;
    }

    public Long getEntidadeId() {
        return entidadeId;
    }

    public String getDetalhe() {
        return detalhe;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }
}
