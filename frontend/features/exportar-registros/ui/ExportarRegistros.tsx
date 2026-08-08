'use client';

import { baixarArquivo } from '@/shared/api/client';

/** Botões de download da exportação dos registros (autenticado via token). */
export function ExportarRegistros() {
  function exportar(formato: 'xlsx' | 'csv') {
    void baixarArquivo('/api/registros/exportar', { formato }, `registros.${formato}`);
  }

  return (
    <section aria-label="Exportar registros">
      <h2>Exportar</h2>
      <p>Baixe todos os registros ativos para conferência ou backup.</p>
      <button type="button" onClick={() => exportar('xlsx')}>
        Exportar .xlsx
      </button>{' '}
      <button type="button" onClick={() => exportar('csv')}>
        Exportar .csv
      </button>
    </section>
  );
}
