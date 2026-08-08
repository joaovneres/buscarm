'use client';

import { useImportarImagem } from '../model/useImportarImagem';
import { ConferenciaOcr } from './ConferenciaOcr';
import { UploadImagem } from './UploadImagem';

export function ImportarImagem() {
  const { etapa, importacao, linhas, erro, processar, editar, alternar, confirmar, reiniciar } =
    useImportarImagem();

  return (
    <section>
      {(etapa === 'idle' || etapa === 'processando' || etapa === 'erro') && (
        <UploadImagem onProcessar={processar} processando={etapa === 'processando'} />
      )}

      {erro && <p role="alert">{erro}</p>}

      {(etapa === 'conferencia' || etapa === 'confirmando') && importacao && (
        <ConferenciaOcr
          importacao={importacao}
          linhas={linhas}
          onEditar={editar}
          onAlternar={alternar}
          onConfirmar={confirmar}
          confirmando={etapa === 'confirmando'}
        />
      )}

      {etapa === 'concluida' && importacao && (
        <section aria-label="Relatório da importação">
          <h2>Importação concluída</h2>
          <ul>
            <li>
              <strong>{importacao.resumo.importados}</strong> registro(s) importado(s)
            </li>
            <li>{importacao.resumo.ignorados} ignorado(s)</li>
          </ul>
          <button type="button" onClick={reiniciar}>
            Importar outra foto
          </button>
        </section>
      )}
    </section>
  );
}
