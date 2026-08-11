package com.buscarm.registro.application.port.in;

import com.buscarm.registro.domain.FiltroRegistros;
import com.buscarm.registro.domain.Registro;
import com.buscarm.shared.Pagina;

/** Porta de entrada: pesquisar registros (nome/RM/status/ano/inicial) e obter por id. */
public interface BuscarRegistrosUseCase {

    Pagina<Registro> pesquisar(FiltroRegistros filtro, int pagina, int tamanho);

    Registro buscarPorId(long id);
}
