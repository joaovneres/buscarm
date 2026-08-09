'use client';

import { useState, type FormEvent } from 'react';

import type { AlteracaoRegistro } from '@/entities/registro/api/alterarRegistro';
import type { RegistroResponse } from '@/entities/registro/model/types';

import { useBuscaRegistros } from '../model/useBuscaRegistros';
import { useGerenciarRegistros } from '../model/useGerenciarRegistros';
import { TabelaResultados } from './TabelaResultados';

type TipoBusca = 'nome' | 'rm';

export function BuscaRegistros() {
  const [tipo, setTipo] = useState<TipoBusca>('nome');
  const [termo, setTermo] = useState('');
  const [editandoId, setEditandoId] = useState<number | null>(null);
  const { estado, resultados, erro, buscar, recarregar } = useBuscaRegistros();
  const { enviando, erro: erroAcao, alterar, desativar } = useGerenciarRegistros();

  function onSubmit(e: FormEvent) {
    e.preventDefault();
    const valor = termo.trim();
    if (!valor) {
      return;
    }
    setEditandoId(null);
    buscar(tipo === 'nome' ? { nome: valor } : { rm: valor });
  }

  async function onSalvar(id: number, dados: AlteracaoRegistro) {
    const ok = await alterar(id, dados);
    if (ok) {
      setEditandoId(null);
      await recarregar();
    }
  }

  async function onDesativar(registro: RegistroResponse) {
    const confirmado = window.confirm(
      `Desativar o registro de RM ${registro.rm} (${registro.nomeCompleto})?`,
    );
    if (!confirmado) {
      return;
    }
    const ok = await desativar(registro.id);
    if (ok) {
      await recarregar();
    }
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
      {estado === 'sucesso' && (
        <TabelaResultados
          registros={resultados}
          editandoId={editandoId}
          enviando={enviando}
          erroAcao={erroAcao}
          onIniciarEdicao={setEditandoId}
          onCancelarEdicao={() => setEditandoId(null)}
          onSalvar={onSalvar}
          onDesativar={onDesativar}
        />
      )}
    </section>
  );
}
