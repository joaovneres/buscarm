package com.buscarm.importacao.application.port.out;

import java.util.Optional;

import com.buscarm.importacao.domain.Importacao;

/** Porta de saída: persistência das importações e seus itens. */
public interface ImportacaoRepositoryPort {

    Importacao salvar(Importacao importacao);

    Optional<Importacao> buscarPorId(long id);
}
