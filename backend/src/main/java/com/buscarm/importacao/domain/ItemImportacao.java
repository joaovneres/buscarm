package com.buscarm.importacao.domain;

/** Um item (linha) de uma importação, com sua classificação e rastreabilidade da origem. */
public class ItemImportacao {

    private final Long id;
    private final int linhaOrigem;
    private final Integer rmExtraido;
    private final String nomeExtraido;
    private StatusItem status;
    private String mensagemErro;
    private Long registroId;

    public ItemImportacao(Long id, int linhaOrigem, Integer rmExtraido, String nomeExtraido,
                          StatusItem status, String mensagemErro, Long registroId) {
        this.id = id;
        this.linhaOrigem = linhaOrigem;
        this.rmExtraido = rmExtraido;
        this.nomeExtraido = nomeExtraido;
        this.status = status;
        this.mensagemErro = mensagemErro;
        this.registroId = registroId;
    }

    public static ItemImportacao novo(RegistroExtraido extraido, StatusItem status, String mensagem) {
        return new ItemImportacao(null, extraido.linhaOrigem(), extraido.rm(),
                extraido.nomeCompleto(), status, mensagem, null);
    }

    /** Marca como importado, ligando ao registro de matrícula criado. */
    public void marcarImportado(long registroId) {
        this.status = StatusItem.IMPORTADO;
        this.registroId = registroId;
        this.mensagemErro = null;
    }

    public void marcarIgnorado(String motivo) {
        this.status = StatusItem.IGNORADO;
        this.mensagemErro = motivo;
    }

    public boolean isValido() {
        return status == StatusItem.VALIDO;
    }

    public Long getId() {
        return id;
    }

    public int getLinhaOrigem() {
        return linhaOrigem;
    }

    public Integer getRmExtraido() {
        return rmExtraido;
    }

    public String getNomeExtraido() {
        return nomeExtraido;
    }

    public StatusItem getStatus() {
        return status;
    }

    public String getMensagemErro() {
        return mensagemErro;
    }

    public Long getRegistroId() {
        return registroId;
    }
}
