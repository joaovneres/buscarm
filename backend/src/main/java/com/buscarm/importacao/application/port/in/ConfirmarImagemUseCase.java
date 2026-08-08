package com.buscarm.importacao.application.port.in;

import java.util.List;

import com.buscarm.importacao.domain.Importacao;

/** Porta de entrada: confirma a importação por imagem, com os dados corrigidos pelo funcionário. */
public interface ConfirmarImagemUseCase {

    Importacao confirmarImagem(long importacaoId, List<ItemCorrigido> itens);

    /** Uma linha aprovada, já com o RM/nome revisados na conferência do OCR. */
    record ItemCorrigido(int linha, Integer rm, String nomeCompleto) {
    }
}
