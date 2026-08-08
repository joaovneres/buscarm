'use client';

import { useState, type FormEvent } from 'react';

export function UploadImagem({
  onProcessar,
  processando,
}: {
  onProcessar: (arquivo: File) => void;
  processando: boolean;
}) {
  const [arquivo, setArquivo] = useState<File | null>(null);

  function onSubmit(e: FormEvent) {
    e.preventDefault();
    if (arquivo) {
      onProcessar(arquivo);
    }
  }

  return (
    <form onSubmit={onSubmit} aria-label="Envio de foto do livro">
      <label htmlFor="foto">Foto da página do livro</label>
      <input
        id="foto"
        name="foto"
        type="file"
        accept="image/*"
        capture="environment"
        onChange={(e) => setArquivo(e.target.files?.[0] ?? null)}
      />
      <button type="submit" disabled={!arquivo || processando}>
        {processando ? 'Reconhecendo…' : 'Processar foto (OCR)'}
      </button>
      {processando && <p role="status">Reconhecendo o texto da foto…</p>}
    </form>
  );
}
