package com.buscarm.importacao.application.port.in;

import com.buscarm.importacao.domain.Importacao;

/** Porta de entrada: valida uma planilha e devolve a pré-visualização (NÃO grava no cadastro). */
public interface ValidarPlanilhaUseCase {

    Importacao validar(String nomeArquivo, byte[] conteudo);
}
