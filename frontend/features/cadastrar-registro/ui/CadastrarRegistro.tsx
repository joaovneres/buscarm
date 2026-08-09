'use client';

import { useState, type FormEvent } from 'react';

import { useCadastrarRegistro } from '../model/useCadastrarRegistro';

/** Formulário de cadastro manual de um registro (RM, nome e ano). */
export function CadastrarRegistro() {
  const [aberto, setAberto] = useState(false);
  const [rm, setRm] = useState('');
  const [nomeCompleto, setNomeCompleto] = useState('');
  const [ano, setAno] = useState('');
  const { enviando, erro, criado, cadastrar, limpar } = useCadastrarRegistro();

  function limparCampos() {
    setRm('');
    setNomeCompleto('');
    setAno('');
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    const rmNum = Number(rm);
    if (!Number.isInteger(rmNum) || rmNum <= 0 || !nomeCompleto.trim()) {
      return;
    }
    const ok = await cadastrar({
      rm: rmNum,
      nomeCompleto: nomeCompleto.trim(),
      anoRegistro: ano.trim() ? Number(ano) : null,
    });
    if (ok) {
      limparCampos();
    }
  }

  function fechar() {
    setAberto(false);
    limparCampos();
    limpar();
  }

  if (!aberto) {
    return (
      <section aria-label="Cadastro manual">
        <button type="button" onClick={() => setAberto(true)}>
          + Novo registro
        </button>
      </section>
    );
  }

  return (
    <section aria-label="Cadastro manual">
      <h2>Novo registro</h2>
      <form onSubmit={onSubmit} aria-label="Cadastrar registro">
        <label htmlFor="novo-rm">RM</label>
        <input
          id="novo-rm"
          value={rm}
          onChange={(e) => setRm(e.target.value)}
          inputMode="numeric"
          autoComplete="off"
          required
        />

        <label htmlFor="novo-nome">Nome (sobrenome, nome)</label>
        <input
          id="novo-nome"
          value={nomeCompleto}
          onChange={(e) => setNomeCompleto(e.target.value)}
          autoComplete="off"
          required
        />

        <label htmlFor="novo-ano">Ano do registro</label>
        <input
          id="novo-ano"
          value={ano}
          onChange={(e) => setAno(e.target.value)}
          inputMode="numeric"
          placeholder="opcional"
        />

        {erro && <p role="alert">{erro}</p>}
        {criado && (
          <p role="status">
            Registro criado: RM {criado.rm} — {criado.nomeCompleto}.
          </p>
        )}

        <button type="submit" disabled={enviando}>
          {enviando ? 'Salvando…' : 'Cadastrar'}
        </button>
        <button type="button" onClick={fechar} disabled={enviando}>
          Fechar
        </button>
      </form>
    </section>
  );
}
