package com.buscarm.importacao.application.port.in;

import com.buscarm.importacao.domain.Importacao;

/** Porta de entrada: consulta uma importação (status/relatório). */
public interface ConsultarImportacaoUseCase {

    Importacao buscarPorId(long id);
}
