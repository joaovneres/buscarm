package com.buscarm.registro.application.port.in;

import com.buscarm.registro.domain.FormatoExportacao;
import com.buscarm.shared.ArquivoExportado;

/** Porta de entrada: exporta todos os registros ativos no formato pedido (.xlsx/.csv). */
public interface ExportarRegistrosUseCase {

    ArquivoExportado exportar(FormatoExportacao formato);
}
