import { apiPost } from '@/shared/api/client';

import type { RegistroResponse } from '../model/types';

/** Dados de um novo registro (espelha o RegistroRequest do backend). */
export interface NovoRegistro {
  rm: number;
  nomeCompleto: string;
  anoRegistro: number | null;
}

/** Cadastra manualmente um novo registro (índice Nome→RM). */
export function cadastrarRegistro(dados: NovoRegistro): Promise<RegistroResponse> {
  return apiPost<RegistroResponse>('/api/registros', dados);
}
