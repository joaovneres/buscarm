import { urlExportacaoRegistros } from '@/entities/registro/api/exportarRegistros';

/** Botões de download da exportação dos registros (backup de negócio). */
export function ExportarRegistros() {
  return (
    <section aria-label="Exportar registros">
      <h2>Exportar</h2>
      <p>Baixe todos os registros ativos para conferência ou backup.</p>
      <a href={urlExportacaoRegistros('xlsx')} download>
        Exportar .xlsx
      </a>{' '}
      <a href={urlExportacaoRegistros('csv')} download>
        Exportar .csv
      </a>
    </section>
  );
}
