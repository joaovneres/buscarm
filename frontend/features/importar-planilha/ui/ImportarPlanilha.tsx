'use client';

import { useImportarPlanilha } from '../model/useImportarPlanilha';
import { PreviewImportacao } from './PreviewImportacao';
import { RelatorioImportacao } from './RelatorioImportacao';
import { UploadPlanilha } from './UploadPlanilha';

export function ImportarPlanilha() {
  const {
    etapa,
    importacao,
    selecionadas,
    erro,
    validar,
    alternarLinha,
    confirmar,
    reiniciar,
  } = useImportarPlanilha();

  return (
    <section>
      {(etapa === 'idle' || etapa === 'validando' || etapa === 'erro') && (
        <UploadPlanilha onValidar={validar} validando={etapa === 'validando'} />
      )}

      {erro && <p role="alert">{erro}</p>}

      {(etapa === 'previa' || etapa === 'confirmando') && importacao && (
        <PreviewImportacao
          importacao={importacao}
          selecionadas={selecionadas}
          onAlternarLinha={alternarLinha}
          onConfirmar={confirmar}
          confirmando={etapa === 'confirmando'}
        />
      )}

      {etapa === 'concluida' && importacao && (
        <RelatorioImportacao importacao={importacao} onReiniciar={reiniciar} />
      )}
    </section>
  );
}
