/** Modelos da entidade Importação no frontend — espelham os DTOs do backend. */
export type SituacaoItem =
  | 'VALIDO'
  | 'INVALIDO'
  | 'DUPLICADO'
  | 'CONFLITO'
  | 'IMPORTADO'
  | 'IGNORADO';

export interface ItemImportacao {
  linha: number;
  rm: number | null;
  nome: string | null;
  situacao: SituacaoItem;
  mensagem: string | null;
  registroId: number | null;
}

export interface ResumoImportacao {
  total: number;
  validos: number;
  duplicados: number;
  conflitos: number;
  invalidos: number;
  importados: number;
  ignorados: number;
}

export interface ImportacaoResponse {
  id: number;
  tipo: string;
  nomeArquivo: string;
  status: 'VALIDADA' | 'CONFIRMADA';
  resumo: ResumoImportacao;
  itens: ItemImportacao[];
}
