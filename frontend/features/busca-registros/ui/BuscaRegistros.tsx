'use client';

import { useState, type FormEvent } from 'react';

import { useBuscaRegistros } from '../model/useBuscaRegistros';
import { TabelaResultados } from './TabelaResultados';

type TipoBusca = 'nome' | 'rm';

export function BuscaRegistros() {
  const [tipo, setTipo] = useState<TipoBusca>('nome');
  const [termo, setTermo] = useState('');
  const { estado, resultados, erro, buscar } = useBuscaRegistros();

  function onSubmit(e: FormEvent) {
    e.preventDefault();
    const valor = termo.trim();
    if (!valor) {
      return;
    }
    buscar(tipo === 'nome' ? { nome: valor } : { rm: valor });
  }

  return (
    <section>
      <form onSubmit={onSubmit} aria-label="Busca de registros">
        <fieldset>
          <legend>Buscar por</legend>
          <label>
            <input
              type="radio"
              name="tipo"
              checked={tipo === 'nome'}
              onChange={() => setTipo('nome')}
            />{' '}
            Nome
          </label>
          <label>
            <input
              type="radio"
              name="tipo"
              checked={tipo === 'rm'}
              onChange={() => setTipo('rm')}
            />{' '}
            RM
          </label>
        </fieldset>

        <label htmlFor="termo">{tipo === 'nome' ? 'Nome (sobrenome, nome)' : 'RM'}</label>
        <input
          id="termo"
          name="termo"
          value={termo}
          onChange={(e) => setTermo(e.target.value)}
          inputMode={tipo === 'rm' ? 'numeric' : 'text'}
          autoComplete="off"
        />

        <button type="submit">Buscar</button>
      </form>

      {estado === 'loading' && <p role="status">Buscando…</p>}
      {estado === 'erro' && <p role="alert">{erro}</p>}
      {estado === 'sucesso' && <TabelaResultados registros={resultados} />}
    </section>
  );
}
