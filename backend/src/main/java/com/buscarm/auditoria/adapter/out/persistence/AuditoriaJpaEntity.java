package com.buscarm.auditoria.adapter.out.persistence;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "auditoria")
public class AuditoriaJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String usuario;

    @Column(nullable = false, length = 50)
    private String acao;

    @Column(nullable = false, length = 50)
    private String entidade;

    @Column(name = "entidade_id")
    private Long entidadeId;

    @Column(length = 500)
    private String detalhe;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    protected AuditoriaJpaEntity() {
    }

    public AuditoriaJpaEntity(String usuario, String acao, String entidade, Long entidadeId,
                              String detalhe) {
        this.usuario = usuario;
        this.acao = acao;
        this.entidade = entidade;
        this.entidadeId = entidadeId;
        this.detalhe = detalhe;
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
