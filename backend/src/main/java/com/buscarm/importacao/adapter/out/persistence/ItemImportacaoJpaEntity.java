package com.buscarm.importacao.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.buscarm.importacao.domain.StatusItem;

@Entity
@Table(name = "importacao_item")
public class ItemImportacaoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "importacao_id", nullable = false)
    private ImportacaoJpaEntity importacao;

    @Column(name = "linha_origem", nullable = false)
    private int linhaOrigem;

    @Column(name = "rm_extraido")
    private Integer rmExtraido;

    @Column(name = "nome_extraido")
    private String nomeExtraido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusItem status;

    @Column(name = "mensagem_erro", length = 500)
    private String mensagemErro;

    @Column(name = "registro_matricula_id")
    private Long registroMatriculaId;

    protected ItemImportacaoJpaEntity() {
    }

    public Long getId() {
        return id;
    }

    public ImportacaoJpaEntity getImportacao() {
        return importacao;
    }

    public void setImportacao(ImportacaoJpaEntity importacao) {
        this.importacao = importacao;
    }

    public int getLinhaOrigem() {
        return linhaOrigem;
    }

    public void setLinhaOrigem(int linhaOrigem) {
        this.linhaOrigem = linhaOrigem;
    }

    public Integer getRmExtraido() {
        return rmExtraido;
    }

    public void setRmExtraido(Integer rmExtraido) {
        this.rmExtraido = rmExtraido;
    }

    public String getNomeExtraido() {
        return nomeExtraido;
    }

    public void setNomeExtraido(String nomeExtraido) {
        this.nomeExtraido = nomeExtraido;
    }

    public StatusItem getStatus() {
        return status;
    }

    public void setStatus(StatusItem status) {
        this.status = status;
    }

    public String getMensagemErro() {
        return mensagemErro;
    }

    public void setMensagemErro(String mensagemErro) {
        this.mensagemErro = mensagemErro;
    }

    public Long getRegistroMatriculaId() {
        return registroMatriculaId;
    }

    public void setRegistroMatriculaId(Long registroMatriculaId) {
        this.registroMatriculaId = registroMatriculaId;
    }
}
