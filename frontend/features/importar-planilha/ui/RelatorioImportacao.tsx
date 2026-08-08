'use client';

import type { ImportacaoResponse } from '@/entities/importacao/model/types';

export function RelatorioImportacao({
  importacao,
  onReiniciar,
}: {
  importacao: ImportacaoResponse;
  onReiniciar: () => void;
}) {
  const { resumo } = importacao;
  return (
    <section aria-label="Relatório da importação">
      <h2>Importação concluída</h2>
      <ul>
        <li>
          <strong>{resumo.importados}</strong> registro(s) importado(s)
        </li>
        <li>{resumo.ignorados} ignorado(s)</li>
        <li>{resumo.duplicados} já cadastrado(s)</li>
        <li>{resumo.conflitos} em conflito</li>
        <li>{resumo.invalidos} inválido(s)</li>
      </ul>
      <button type="button" onClick={onReiniciar}>
        Importar outra planilha
      </button>
    </section>
  );
}
