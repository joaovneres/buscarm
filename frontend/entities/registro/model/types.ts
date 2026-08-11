/** Modelo da entidade Registro no frontend — espelha os DTOs do backend. */

export type StatusValidacao = 'PENDENTE' | 'VALIDADO';

export interface RegistroResponse {
  id: number;
  rm: number;
  nomeCompleto: string;
  sobrenome: string;
  anoRegistro: number | null;
  ativo: boolean;
  status: StatusValidacao;
  criadoPor: string;
  atualizadoPor: string;
  criadoEm: string;
  atualizadoEm: string | null;
}

export interface PaginaResponse<T> {
  conteudo: T[];
  pagina: number;
  tamanho: number;
  total: number;
  totalPaginas: number;
}

/** Filtros da busca (todos opcionais). */
export interface FiltrosBusca {
  nome?: string;
  rm?: string;
  status?: StatusValidacao;
  ano?: string;
  inicial?: string;
  pagina?: number;
  tamanho?: number;
}
