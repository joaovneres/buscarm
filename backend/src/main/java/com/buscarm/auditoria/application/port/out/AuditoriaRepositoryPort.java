package com.buscarm.auditoria.application.port.out;

import com.buscarm.auditoria.domain.RegistroAuditoria;
import com.buscarm.shared.Pagina;

/** Porta de saída: persistência da auditoria. */
public interface AuditoriaRepositoryPort {

    RegistroAuditoria salvar(RegistroAuditoria registro);

    Pagina<RegistroAuditoria> listar(int pagina, int tamanho);
}
