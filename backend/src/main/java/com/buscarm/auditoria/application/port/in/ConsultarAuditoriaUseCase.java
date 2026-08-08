package com.buscarm.auditoria.application.port.in;

import com.buscarm.auditoria.domain.RegistroAuditoria;
import com.buscarm.shared.Pagina;

/** Porta de entrada: consulta o histórico de auditoria (uso administrativo). */
public interface ConsultarAuditoriaUseCase {

    Pagina<RegistroAuditoria> listar(int pagina, int tamanho);
}
