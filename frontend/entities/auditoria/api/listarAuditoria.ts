import { apiGet } from '@/shared/api/client';

import type { PaginaAuditoria } from '../model/types';

export function listarAuditoria(pagina = 0, tamanho = 50): Promise<PaginaAuditoria> {
  return apiGet<PaginaAuditoria>('/api/auditoria', {
    pagina: String(pagina),
    tamanho: String(tamanho),
  });
}
