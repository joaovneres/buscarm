package com.buscarm.importacao.application.port.in;

import java.util.List;

import com.buscarm.importacao.domain.Importacao;

/** Porta de entrada: confirma a importação, gravando apenas os itens válidos aprovados. */
public interface ConfirmarImportacaoUseCase {

    /**
     * @param importacaoId    importação previamente validada
     * @param linhasAprovadas linhas de origem que o funcionário aprovou para gravação
     */
    Importacao confirmar(long importacaoId, List<Integer> linhasAprovadas);
}
