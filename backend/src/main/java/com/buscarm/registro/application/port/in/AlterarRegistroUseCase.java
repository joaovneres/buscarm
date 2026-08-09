package com.buscarm.registro.application.port.in;

import com.buscarm.registro.domain.Registro;

/** Porta de entrada: alterar os dados de um registro ou desativá-lo (exclusão lógica). */
public interface AlterarRegistroUseCase {

    Registro alterar(long id, AlterarRegistroCommand comando);

    void desativar(long id);

    /** Comando de alteração (independente do DTO web). */
    record AlterarRegistroCommand(int rm, String nomeCompleto, Integer anoRegistro) {
    }
}
