import { apiPost } from '@/shared/api/client';

import type { ImportacaoResponse } from '../model/types';

export interface ItemCorrigido {
  linha: number;
  rm: number | null;
  nomeCompleto: string;
}

/** Confirma a importação por imagem com os dados revisados na conferência. */
export function confirmarImagem(
  importacaoId: number,
  itens: ItemCorrigido[],
): Promise<ImportacaoResponse> {
  return apiPost<ImportacaoResponse>('/api/importacoes/imagem/confirmar', {
    importacaoId,
    itens,
  });
}
