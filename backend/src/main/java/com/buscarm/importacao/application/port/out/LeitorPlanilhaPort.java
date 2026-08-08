package com.buscarm.importacao.application.port.out;

import java.util.List;

import com.buscarm.importacao.domain.RegistroExtraido;

/** Porta de saída: extrai registros de uma planilha (implementada por um adaptador Apache POI). */
public interface LeitorPlanilhaPort {

    List<RegistroExtraido> ler(byte[] conteudo);
}
