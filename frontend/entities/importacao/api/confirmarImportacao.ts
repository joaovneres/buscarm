import { apiPost } from '@/shared/api/client';

import type { ImportacaoResponse } from '../model/types';

/** Confirma a importação, gravando apenas as linhas válidas aprovadas. */
export function confirmarImportacao(
  importacaoId: number,
  linhasAprovadas: number[],
): Promise<ImportacaoResponse> {
  return apiPost<ImportacaoResponse>('/api/importacoes/planilha/confirmar', {
    importacaoId,
    linhasAprovadas,
  });
}
