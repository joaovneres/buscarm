'use client';

import type { ImportacaoResponse } from '@/entities/importacao/model/types';

import type { MapaLinhas } from '../model/useImportarImagem';

export function ConferenciaOcr({
  importacao,
  linhas,
  onEditar,
  onAlternar,
  onConfirmar,
  confirmando,
}: {
  importacao: ImportacaoResponse;
  linhas: MapaLinhas;
  onEditar: (linha: number, campo: 'rm' | 'nome', valor: string) => void;
  onAlternar: (linha: number) => void;
  onConfirmar: () => void;
  confirmando: boolean;
}) {
  const selecionadas = Object.values(linhas).filter((l) => l.selecionado).length;

  return (
    <section aria-label="Conferência do OCR">
      <h2>Conferência — {importacao.nomeArquivo}</h2>
      <p>Revise o RM e o nome reconhecidos antes de gravar. Corrija onde o OCR errou.</p>

      <table>
        <caption>Texto reconhecido na foto</caption>
        <thead>
          <tr>
            <th scope="col">Importar</th>
            <th scope="col">Linha</th>
            <th scope="col">RM</th>
            <th scope="col">Nome</th>
            <th scope="col">OCR</th>
          </tr>
        </thead>
        <tbody>
          {importacao.itens.map((item) => {
            const linha = linhas[item.linha];
            return (
              <tr key={item.linha}>
                <td>
                  <input
                    type="checkbox"
                    aria-label={`Importar linha ${item.linha}`}
                    checked={linha?.selecionado ?? false}
                    onChange={() => onAlternar(item.linha)}
                  />
                </td>
                <td>{item.linha}</td>
                <td>
                  <input
                    aria-label={`RM da linha ${item.linha}`}
                    inputMode="numeric"
                    value={linha?.rm ?? ''}
                    onChange={(e) => onEditar(item.linha, 'rm', e.target.value)}
                  />
                </td>
                <td>
                  <input
                    aria-label={`Nome da linha ${item.linha}`}
                    value={linha?.nome ?? ''}
                    onChange={(e) => onEditar(item.linha, 'nome', e.target.value)}
                  />
                </td>
                <td>{item.situacao === 'VALIDO' ? '✓' : '⚠ conferir'}</td>
              </tr>
            );
          })}
        </tbody>
      </table>

      <button type="button" onClick={onConfirmar} disabled={confirmando || selecionadas === 0}>
        {confirmando ? 'Importando…' : `Importar ${selecionadas} registro(s)`}
      </button>
    </section>
  );
}
