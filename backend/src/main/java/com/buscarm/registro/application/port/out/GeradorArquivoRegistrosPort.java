package com.buscarm.registro.application.port.out;

import java.util.List;

import com.buscarm.registro.domain.FormatoExportacao;
import com.buscarm.registro.domain.Registro;
import com.buscarm.shared.ArquivoExportado;

/** Porta de saída: gera o arquivo de exportação a partir dos registros (implementada por infra). */
public interface GeradorArquivoRegistrosPort {

    ArquivoExportado gerar(List<Registro> registros, FormatoExportacao formato);
}
