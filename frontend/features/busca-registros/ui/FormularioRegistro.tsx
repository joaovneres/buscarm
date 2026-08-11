'use client';

import { useState, type FormEvent } from 'react';

import type { RegistroResponse } from '@/entities/registro/model/types';

export interface DadosRegistro {
  rm: number;
  nomeCompleto: string;
  anoRegistro: number | null;
}

interface Props {
  /** Quando presente, o formulário edita (campos preenchidos); ausente = cadastro novo. */
  registro?: RegistroResponse;
  enviando: boolean;
  erro: string | null;
  onSalvar: (dados: DadosRegistro) => void;
  onCancelar: () => void;
}

/** Formulário único de cadastro/edição de registro (RM, nome e ano). */
export function FormularioRegistro({ registro, enviando, erro, onSalvar, onCancelar }: Props) {
  const [rm, setRm] = useState(registro ? String(registro.rm) : '');
  const [nomeCompleto, setNomeCompleto] = useState(registro?.nomeCompleto ?? '');
  const [ano, setAno] = useState(
    registro?.anoRegistro != null ? String(registro.anoRegistro) : '',
  );

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
    <form onSubmit={onSubmit} aria-label={registro ? 'Editar registro' : 'Cadastrar registro'}>
      <label htmlFor="reg-rm">RM</label>
      <input
        id="reg-rm"
        value={rm}
        onChange={(e) => setRm(e.target.value)}
        inputMode="numeric"
        autoComplete="off"
        required
      />

      <label htmlFor="reg-nome">Nome (sobrenome, nome)</label>
      <input
        id="reg-nome"
        value={nomeCompleto}
        onChange={(e) => setNomeCompleto(e.target.value)}
        autoComplete="off"
        required
      />

      <label htmlFor="reg-ano">Ano do registro</label>
      <input
        id="reg-ano"
        value={ano}
        onChange={(e) => setAno(e.target.value)}
        inputMode="numeric"
        placeholder="opcional"
      />

      {erro && <p role="alert">{erro}</p>}

      <div className="modal__acoes">
        <button type="submit" disabled={enviando}>
          {enviando ? 'Salvando…' : registro ? 'Salvar alterações' : 'Cadastrar'}
        </button>
        <button type="button" className="btn-secundario" onClick={onCancelar} disabled={enviando}>
          Cancelar
        </button>
      </div>
    </form>
  );
}
