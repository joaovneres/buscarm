'use client';

import { useState, type FormEvent } from 'react';

export function UploadPlanilha({
  onValidar,
  validando,
}: {
  onValidar: (arquivo: File) => void;
  validando: boolean;
}) {
  const [arquivo, setArquivo] = useState<File | null>(null);

  function onSubmit(e: FormEvent) {
    e.preventDefault();
    if (arquivo) {
      onValidar(arquivo);
    }
  }

  return (
    <form onSubmit={onSubmit} aria-label="Envio de planilha">
      <label htmlFor="arquivo">Planilha de registros (.xlsx ou .csv)</label>
      <input
        id="arquivo"
        name="arquivo"
        type="file"
        accept=".xlsx,.csv,text/csv,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        onChange={(e) => setArquivo(e.target.files?.[0] ?? null)}
        aria-describedby="arquivo-ajuda"
      />
      <p id="arquivo-ajuda">
        A planilha precisa ter as colunas <strong>Nome</strong> e <strong>RM</strong>. Em CSV, aceita
        separador vírgula ou ponto e vírgula.
      </p>
      <button type="submit" disabled={!arquivo || validando}>
        {validando ? 'Validando…' : 'Validar planilha'}
      </button>
      {validando && <p role="status">Lendo e validando a planilha…</p>}
    </form>
  );
}
