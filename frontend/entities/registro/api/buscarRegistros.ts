import { apiGet } from '@/shared/api/client';

import type { FiltrosBusca, PaginaResponse, RegistroResponse } from '../model/types';

/** Busca paginada de registros com filtros (nome/RM/status/ano/inicial). */
export function buscarRegistros(
  filtros: FiltrosBusca,
): Promise<PaginaResponse<RegistroResponse>> {
  const query: Record<string, string> = {};
  if (filtros.nome) query.nome = filtros.nome;
  if (filtros.rm) query.rm = filtros.rm;
  if (filtros.status) query.status = filtros.status;
  if (filtros.ano) query.ano = filtros.ano;
  if (filtros.inicial) query.inicial = filtros.inicial;
  query.pagina = String(filtros.pagina ?? 0);
  query.tamanho = String(filtros.tamanho ?? 20);
  return apiGet<PaginaResponse<RegistroResponse>>('/api/registros', query);
}
