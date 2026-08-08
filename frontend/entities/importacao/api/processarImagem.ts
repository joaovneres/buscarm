import { apiPostForm } from '@/shared/api/client';

import type { ImportacaoResponse } from '../model/types';

/** Envia a foto para o OCR e recebe a conferência (não grava no cadastro). */
export function processarImagem(arquivo: File): Promise<ImportacaoResponse> {
  const form = new FormData();
  form.append('arquivo', arquivo);
  return apiPostForm<ImportacaoResponse>('/api/importacoes/imagem/processar', form);
}
