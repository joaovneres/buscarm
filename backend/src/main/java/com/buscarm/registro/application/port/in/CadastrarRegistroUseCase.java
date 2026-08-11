package com.buscarm.registro.application.port.in;

import com.buscarm.registro.domain.Registro;
import com.buscarm.registro.domain.StatusValidacao;

/** Porta de entrada: cadastrar um registro manualmente. */
public interface CadastrarRegistroUseCase {

    Registro cadastrar(CadastrarRegistroCommand comando);

    /** Comando de entrada da aplicação (independente do DTO web). */
    record CadastrarRegistroCommand(int rm, String nomeCompleto, Integer anoRegistro, StatusValidacao status) {

        /** Cadastro manual: nasce VALIDADO. */
        public CadastrarRegistroCommand(int rm, String nomeCompleto, Integer anoRegistro) {
            this(rm, nomeCompleto, anoRegistro, StatusValidacao.VALIDADO);
        }
    }
}
