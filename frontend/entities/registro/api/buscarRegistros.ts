import { apiGet } from '@/shared/api/client';

import type { PaginaResponse, RegistroResponse } from '../model/types';

/** Acesso a dados da entidade Registro: busca por nome (acento-insensível) ou por RM. */
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
