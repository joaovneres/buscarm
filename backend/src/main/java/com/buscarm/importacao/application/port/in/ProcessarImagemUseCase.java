package com.buscarm.importacao.application.port.in;

import com.buscarm.importacao.domain.Importacao;

/** Porta de entrada: processa a foto (OCR) e devolve a conferência (NÃO grava no cadastro). */
public interface ProcessarImagemUseCase {

    Importacao processar(String nomeArquivo, byte[] imagem);
}
