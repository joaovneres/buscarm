'use client';

import { useState, type FormEvent } from 'react';

import type { FiltrosBusca, StatusValidacao } from '@/entities/registro/model/types';

const LETRAS = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'.split('');

interface Props {
  onAplicar: (filtros: FiltrosBusca) => void;
}

/** Busca por nome/RM + filtros (status, ano, inicial). Números viram busca por RM. */
export function BarraBusca({ onAplicar }: Props) {
  const [busca, setBusca] = useState('');
  const [status, setStatus] = useState('');
  const [ano, setAno] = useState('');
  const [inicial, setInicial] = useState('');

  function montar(): FiltrosBusca {
    const termo = busca.trim();
    const ehRm = termo.length > 0 && /^\d+$/.test(termo);
    return {
      nome: ehRm ? undefined : termo || undefined,
      rm: ehRm ? termo : undefined,
      status: (status || undefined) as StatusValidacao | undefined,
      ano: ano.trim() || undefined,
      inicial: inicial || undefined,
    };
  }

  function onSubmit(e: FormEvent) {
    e.preventDefault();
    onAplicar(montar());
  }

  function limpar() {
    setBusca('');
    setStatus('');
    setAno('');
    setInicial('');
    onAplicar({});
  }

  return (
    <form className="barra-busca" onSubmit={onSubmit} aria-label="Buscar registros">
      <div className="barra-busca__linha">
        <div className="campo-busca">
          <label htmlFor="busca">Nome ou RM</label>
          <input
            id="busca"
            value={busca}
            onChange={(e) => setBusca(e.target.value)}
            placeholder="Ex.: Souza, Ana — ou 203"
            autoComplete="off"
          />
        </div>
        <button type="submit">Buscar</button>
      </div>

      <div className="filtros">
        <label className="filtro">
          <span>Status</span>
          <select value={status} onChange={(e) => setStatus(e.target.value)}>
            <option value="">Todos</option>
            <option value="PENDENTE">Pendente</option>
            <option value="VALIDADO">Validado</option>
          </select>
        </label>

        <label className="filtro">
          <span>Ano</span>
          <input
            value={ano}
            onChange={(e) => setAno(e.target.value)}
            inputMode="numeric"
            placeholder="Todos"
          />
        </label>

        <label className="filtro">
          <span>Inicial</span>
          <select value={inicial} onChange={(e) => setInicial(e.target.value)}>
            <option value="">Todas</option>
            {LETRAS.map((l) => (
              <option key={l} value={l}>
                {l}
              </option>
            ))}
          </select>
        </label>

        <button type="button" className="btn-secundario" onClick={limpar}>
          Limpar
        </button>
      </div>
    </form>
  );
}
