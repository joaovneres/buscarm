package com.buscarm.registro.application.port.in;

import com.buscarm.registro.domain.Registro;
import com.buscarm.shared.Pagina;

/** Porta de entrada: pesquisar registros por nome ou RM, e obter por id. */
public interface BuscarRegistrosUseCase {

    Pagina<Registro> pesquisar(String nome, Integer rm, int pagina, int tamanho);

    Registro buscarPorId(long id);
}
