import { urlApi } from '@/shared/api/client';

export type FormatoExportacao = 'xlsx' | 'csv';

/** URL de download da exportação de todos os registros ativos. */
export function urlExportacaoRegistros(formato: FormatoExportacao): string {
  return urlApi('/api/registros/exportar', { formato });
}
