import { apiDelete, apiPut } from '@/shared/api/client';

import type { RegistroResponse } from '../model/types';

/** Dados editáveis de um registro (espelha o RegistroRequest do backend). */
export interface AlteracaoRegistro {
  rm: number;
  nomeCompleto: string;
  anoRegistro: number | null;
}

/** Altera os dados de um registro existente. */
export function alterarRegistro(
  id: number,
  dados: AlteracaoRegistro,
): Promise<RegistroResponse> {
  return apiPut<RegistroResponse>(`/api/registros/${id}`, dados);
}

/** Desativa (exclusão lógica) um registro — o prontuário físico continua existindo. */
export function desativarRegistro(id: number): Promise<void> {
  return apiDelete(`/api/registros/${id}`);
}
