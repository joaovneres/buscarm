import { apiGet } from '@/lib/api';

import type { PaginaResponse, RegistroResponse } from './types';

/** Busca registros por nome (parcial, acento-insensível) ou por RM. */
export function buscarRegistros(
  params: { nome?: string; rm?: string },
): Promise<PaginaResponse<RegistroResponse>> {
  const query: Record<string, string> = {};
  if (params.nome) {
    query.nome = params.nome;
  }
  if (params.rm) {
    query.rm = params.rm;
  }
  return apiGet<PaginaResponse<RegistroResponse>>('/api/registros', query);
}
