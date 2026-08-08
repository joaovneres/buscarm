/** Modelo da entidade Registro no frontend — espelha os DTOs do backend. */
export interface RegistroResponse {
  id: number;
  rm: number;
  nomeCompleto: string;
  sobrenome: string;
  anoRegistro: number | null;
  ativo: boolean;
  criadoPor: string;
  atualizadoPor: string;
  criadoEm: string;
}

export interface PaginaResponse<T> {
  conteudo: T[];
  pagina: number;
  tamanho: number;
  total: number;
  totalPaginas: number;
}
