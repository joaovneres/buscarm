package com.buscarm.registro.application.port.in;

import com.buscarm.registro.domain.Registro;

/** Porta de entrada: alterar os dados de um registro ou desativá-lo (exclusão lógica). */
public interface AlterarRegistroUseCase {

    Registro alterar(long id, AlterarRegistroCommand comando);

    void desativar(long id);

    /** Marca o registro como validado (conclui a conferência de um item pendente). */
    Registro validar(long id);

    /** Comando de alteração (independente do DTO web). */
    record AlterarRegistroCommand(int rm, String nomeCompleto, Integer anoRegistro) {
    }
}
