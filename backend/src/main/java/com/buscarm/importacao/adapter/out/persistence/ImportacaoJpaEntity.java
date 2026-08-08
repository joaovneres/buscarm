package com.buscarm.importacao.adapter.out.persistence;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.buscarm.importacao.domain.StatusImportacao;
import com.buscarm.importacao.domain.TipoImportacao;

@Entity
@Table(name = "importacao")
@EntityListeners(AuditingEntityListener.class)
public class ImportacaoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoImportacao tipo;

    @Column(name = "nome_arquivo", nullable = false)
    private String nomeArquivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusImportacao status;

    @Column(name = "quantidade_total", nullable = false)
    private int quantidadeTotal;

    @Column(name = "quantidade_importada", nullable = false)
    private int quantidadeImportada;

    @Column(name = "quantidade_ignorada", nullable = false)
    private int quantidadeIgnorada;

    @Column(name = "quantidade_com_erro", nullable = false)
    private int quantidadeComErro;

    @OneToMany(mappedBy = "importacao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemImportacaoJpaEntity> itens = new ArrayList<>();

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

    protected ImportacaoJpaEntity() {
    }

    public void adicionarItem(ItemImportacaoJpaEntity item) {
        item.setImportacao(this);
        this.itens.add(item);
    }

    public Long getId() {
        return id;
    }

    public TipoImportacao getTipo() {
        return tipo;
    }

    public void setTipo(TipoImportacao tipo) {
        this.tipo = tipo;
    }

    public String getNomeArquivo() {
        return nomeArquivo;
    }

    public void setNomeArquivo(String nomeArquivo) {
        this.nomeArquivo = nomeArquivo;
    }

    public StatusImportacao getStatus() {
        return status;
    }

    public void setStatus(StatusImportacao status) {
        this.status = status;
    }

    public int getQuantidadeTotal() {
        return quantidadeTotal;
    }

    public void setQuantidadeTotal(int quantidadeTotal) {
        this.quantidadeTotal = quantidadeTotal;
    }

    public int getQuantidadeImportada() {
        return quantidadeImportada;
    }

    public void setQuantidadeImportada(int quantidadeImportada) {
        this.quantidadeImportada = quantidadeImportada;
    }

    public int getQuantidadeIgnorada() {
        return quantidadeIgnorada;
    }

    public void setQuantidadeIgnorada(int quantidadeIgnorada) {
        this.quantidadeIgnorada = quantidadeIgnorada;
    }

    public int getQuantidadeComErro() {
        return quantidadeComErro;
    }

    public void setQuantidadeComErro(int quantidadeComErro) {
        this.quantidadeComErro = quantidadeComErro;
    }

    public List<ItemImportacaoJpaEntity> getItens() {
        return itens;
    }

    public String getCriadoPor() {
        return criadoPor;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }
}
