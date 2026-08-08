export interface RegistroAuditoria {
  id: number;
  usuario: string;
  acao: string;
  entidade: string;
  entidadeId: number | null;
  detalhe: string | null;
  criadoEm: string;
}

export interface PaginaAuditoria {
  conteudo: RegistroAuditoria[];
  pagina: number;
  tamanho: number;
  total: number;
  totalPaginas: number;
}
