package com.buscarm.importacao.application.port.out;

import java.util.List;

import com.buscarm.importacao.domain.RegistroExtraido;

/** Porta de saída: extrai registros de uma imagem (OCR). Implementada por um adaptador de infra. */
public interface ExtratorImagemPort {

    List<RegistroExtraido> extrair(byte[] imagem);
}
