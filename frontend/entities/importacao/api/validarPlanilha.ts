import { apiPostForm } from '@/shared/api/client';

import type { ImportacaoResponse } from '../model/types';

/** Envia a planilha para validação (pré-visualização) — não grava no cadastro. */
export function validarPlanilha(arquivo: File): Promise<ImportacaoResponse> {
  const form = new FormData();
  form.append('arquivo', arquivo);
  return apiPostForm<ImportacaoResponse>('/api/importacoes/planilha/validar', form);
}
