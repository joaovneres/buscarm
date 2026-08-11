package com.buscarm.registro.adapter.out.persistence;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/** Adaptador de persistência: mapeamento JPA da tabela registro_matricula. */
@Entity
@Table(name = "registro_matricula")
@EntityListeners(AuditingEntityListener.class)
public class RegistroJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Integer rm;

    @Column(name = "nome_completo", nullable = false)
    private String nomeCompleto;

    @Column(nullable = false)
    private String sobrenome;

    @Column(name = "nome_busca", nullable = false)
    private String nomeBusca;

    @Column(name = "ano_registro")
    private Integer anoRegistro;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(nullable = false, length = 20)
    private String status = "VALIDADO";

    @CreatedBy
    @Column(name = "criado_por", nullable = false, updatable = false)
    private String criadoPor;

    @LastModifiedBy
    @Column(name = "atualizado_por", nullable = false)
    private String atualizadoPor;

    @CreatedDate
    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @LastModifiedDate
    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    protected RegistroJpaEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getRm() {
        return rm;
    }

    public void setRm(Integer rm) {
        this.rm = rm;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }

    public String getSobrenome() {
        return sobrenome;
    }

    public void setSobrenome(String sobrenome) {
        this.sobrenome = sobrenome;
    }

    public String getNomeBusca() {
        return nomeBusca;
    }

    public void setNomeBusca(String nomeBusca) {
        this.nomeBusca = nomeBusca;
    }

    public Integer getAnoRegistro() {
        return anoRegistro;
    }

    public void setAnoRegistro(Integer anoRegistro) {
        this.anoRegistro = anoRegistro;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
