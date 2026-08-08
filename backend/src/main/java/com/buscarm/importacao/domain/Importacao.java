package com.buscarm.importacao.domain;

import java.time.Instant;
import java.util.List;

/** Agregado da importação: cabeçalho + itens + contadores do resultado. */
public class Importacao {

    private final Long id;
    private final TipoImportacao tipo;
    private final String nomeArquivo;
    private StatusImportacao status;
    private final List<ItemImportacao> itens;
    private final String criadoPor;
    private final Instant criadoEm;

    public Importacao(Long id, TipoImportacao tipo, String nomeArquivo, StatusImportacao status,
                      List<ItemImportacao> itens, String criadoPor, Instant criadoEm) {
        this.id = id;
        this.tipo = tipo;
        this.nomeArquivo = nomeArquivo;
        this.status = status;
        this.itens = itens;
        this.criadoPor = criadoPor;
        this.criadoEm = criadoEm;
    }

    /** Cria uma importação recém-validada (ainda não gravada no cadastro). */
    public static Importacao validada(TipoImportacao tipo, String nomeArquivo, List<ItemImportacao> itens) {
        return new Importacao(null, tipo, nomeArquivo, StatusImportacao.VALIDADA, itens, null, null);
    }

    public void confirmar() {
        this.status = StatusImportacao.CONFIRMADA;
    }

    public long contar(StatusItem status) {
        return itens.stream().filter(i -> i.getStatus() == status).count();
    }

    public int getQuantidadeTotal() {
        return itens.size();
    }

    public Long getId() {
        return id;
    }

    public TipoImportacao getTipo() {
        return tipo;
    }

    public String getNomeArquivo() {
        return nomeArquivo;
    }

    public StatusImportacao getStatus() {
        return status;
    }

    public List<ItemImportacao> getItens() {
        return itens;
    }

    public String getCriadoPor() {
        return criadoPor;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }
}
