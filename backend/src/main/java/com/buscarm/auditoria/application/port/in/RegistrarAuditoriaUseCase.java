package com.buscarm.auditoria.application.port.in;

/** Porta de entrada: registra uma ação sensível na auditoria (usuário resolvido da sessão). */
public interface RegistrarAuditoriaUseCase {

    void registrar(String acao, String entidade, Long entidadeId, String detalhe);
}
