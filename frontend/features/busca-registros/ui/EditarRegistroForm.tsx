'use client';

import { useState, type FormEvent } from 'react';

import type { AlteracaoRegistro } from '@/entities/registro/api/alterarRegistro';
import type { RegistroResponse } from '@/entities/registro/model/types';

interface Props {
  registro: RegistroResponse;
  enviando: boolean;
  erro: string | null;
  onSalvar: (dados: AlteracaoRegistro) => void;
  onCancelar: () => void;
}

/** Formulário de edição de um registro (RM, nome e ano). */
export function EditarRegistroForm({ registro, enviando, erro, onSalvar, onCancelar }: Props) {
  const [rm, setRm] = useState(String(registro.rm));
  const [nomeCompleto, setNomeCompleto] = useState(registro.nomeCompleto);
  const [ano, setAno] = useState(registro.anoRegistro != null ? String(registro.anoRegistro) : '');

  function onSubmit(e: FormEvent) {
    e.preventDefault();
    const rmNum = Number(rm);
    if (!Number.isInteger(rmNum) || rmNum <= 0 || !nomeCompleto.trim()) {
      return;
    }
    onSalvar({
      rm: rmNum,
      nomeCompleto: nomeCompleto.trim(),
      anoRegistro: ano.trim() ? Number(ano) : null,
    });
  }

  return (
    <form onSubmit={onSubmit} aria-label={`Editar registro RM ${registro.rm}`}>
      <label htmlFor={`rm-${registro.id}`}>RM</label>
      <input
        id={`rm-${registro.id}`}
        value={rm}
        onChange={(e) => setRm(e.target.value)}
        inputMode="numeric"
        required
      />

      <label htmlFor={`nome-${registro.id}`}>Nome (sobrenome, nome)</label>
      <input
        id={`nome-${registro.id}`}
        value={nomeCompleto}
        onChange={(e) => setNomeCompleto(e.target.value)}
        required
      />

      <label htmlFor={`ano-${registro.id}`}>Ano do registro</label>
      <input
        id={`ano-${registro.id}`}
        value={ano}
        onChange={(e) => setAno(e.target.value)}
        inputMode="numeric"
        placeholder="opcional"
      />

      {erro && <p role="alert">{erro}</p>}

      <button type="submit" disabled={enviando}>
        {enviando ? 'Salvando…' : 'Salvar'}
      </button>
      <button type="button" onClick={onCancelar} disabled={enviando}>
        Cancelar
      </button>
    </form>
  );
}
