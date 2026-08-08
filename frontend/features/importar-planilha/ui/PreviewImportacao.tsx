'use client';

import type { ImportacaoResponse, SituacaoItem } from '@/entities/importacao/model/types';

const ROTULO: Record<SituacaoItem, string> = {
  VALIDO: '✓ Válido',
  INVALIDO: '✗ Inválido',
  DUPLICADO: '⚠ Já cadastrado',
  CONFLITO: '⚠ Conflito — revisar',
  IMPORTADO: '✓ Importado',
  IGNORADO: '– Ignorado',
};

export function PreviewImportacao({
  importacao,
  selecionadas,
  onAlternarLinha,
  onConfirmar,
  confirmando,
}: {
  importacao: ImportacaoResponse;
  selecionadas: number[];
  onAlternarLinha: (linha: number) => void;
  onConfirmar: () => void;
  confirmando: boolean;
}) {
  const { resumo, itens } = importacao;

  return (
    <section aria-label="Pré-visualização da importação">
      <h2>Pré-visualização — {importacao.nomeArquivo}</h2>
      <p>
        {resumo.total} linhas · <strong>{resumo.validos}</strong> válidas ·{' '}
        {resumo.duplicados} já cadastradas · {resumo.conflitos} em conflito ·{' '}
        {resumo.invalidos} inválidas
      </p>

      <table>
        <caption>Registros identificados na planilha</caption>
        <thead>
          <tr>
            <th scope="col">Importar</th>
            <th scope="col">Linha</th>
            <th scope="col">RM</th>
            <th scope="col">Nome</th>
            <th scope="col">Situação</th>
          </tr>
        </thead>
        <tbody>
          {itens.map((item) => (
            <tr key={item.linha}>
              <td>
                <input
                  type="checkbox"
                  aria-label={`Importar linha ${item.linha}`}
                  disabled={item.situacao !== 'VALIDO'}
                  checked={selecionadas.includes(item.linha)}
                  onChange={() => onAlternarLinha(item.linha)}
                />
              </td>
              <td>{item.linha}</td>
              <td>{item.rm ?? '—'}</td>
              <td>{item.nome ?? '—'}</td>
              <td>
                {ROTULO[item.situacao]}
                {item.mensagem ? ` (${item.mensagem})` : ''}
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      <button type="button" onClick={onConfirmar} disabled={confirmando || selecionadas.length === 0}>
        {confirmando
          ? 'Importando…'
          : `Importar ${selecionadas.length} registro(s) selecionado(s)`}
      </button>
    </section>
  );
}
