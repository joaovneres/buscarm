'use client';

import { useAuditoria } from '../model/useAuditoria';
import { TabelaAuditoria } from './TabelaAuditoria';

export function ConsultarAuditoria() {
  const { estado, itens, erro } = useAuditoria();

  if (estado === 'loading') {
    return <p role="status">Carregando auditoria…</p>;
  }
  if (estado === 'erro') {
    return <p role="alert">{erro}</p>;
  }
  return <TabelaAuditoria itens={itens} />;
}
