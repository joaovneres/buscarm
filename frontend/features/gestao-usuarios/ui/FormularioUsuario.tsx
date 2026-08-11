'use client';

import { useState, type FormEvent } from 'react';

import type { NovoUsuario } from '@/entities/usuario/model/types';
import type { Papel } from '@/shared/auth/roles';

interface Props {
  enviando: boolean;
  erro: string | null;
  onSalvar: (dados: NovoUsuario) => void;
  onCancelar: () => void;
}

const PAPEIS: { valor: Papel; rotulo: string }[] = [
  { valor: 'VISUALIZADOR', rotulo: 'Visualização (só consulta)' },
  { valor: 'SECRETARIA', rotulo: 'Acesso completo (CRUD de alunos)' },
  { valor: 'ADMIN', rotulo: 'Administrador (tudo + usuários)' },
];

export function FormularioUsuario({ enviando, erro, onSalvar, onCancelar }: Props) {
  const [nome, setNome] = useState('');
  const [username, setUsername] = useState('');
  const [senha, setSenha] = useState('');
  const [papel, setPapel] = useState<Papel>('VISUALIZADOR');

  function onSubmit(e: FormEvent) {
    e.preventDefault();
    if (!nome.trim() || !username.trim() || senha.length < 8) {
      return;
    }
    onSalvar({ nome: nome.trim(), username: username.trim(), senha, papel });
  }

  return (
    <form onSubmit={onSubmit} aria-label="Cadastrar usuário">
      <label htmlFor="u-nome">Nome</label>
      <input id="u-nome" value={nome} onChange={(e) => setNome(e.target.value)} required />

      <label htmlFor="u-username">Usuário (para login)</label>
      <input
        id="u-username"
        value={username}
        onChange={(e) => setUsername(e.target.value)}
        autoComplete="off"
        placeholder="ex.: maria.silva"
        required
      />

      <label htmlFor="u-senha">Senha provisória (mín. 8 caracteres)</label>
      <input
        id="u-senha"
        type="text"
        value={senha}
        onChange={(e) => setSenha(e.target.value)}
        autoComplete="off"
        required
      />

      <label htmlFor="u-papel">Papel</label>
      <select id="u-papel" value={papel} onChange={(e) => setPapel(e.target.value as Papel)}>
        {PAPEIS.map((p) => (
          <option key={p.valor} value={p.valor}>
            {p.rotulo}
          </option>
        ))}
      </select>

      {erro && <p role="alert">{erro}</p>}

      <div className="modal__acoes">
        <button type="submit" disabled={enviando}>
          {enviando ? 'Salvando…' : 'Cadastrar usuário'}
        </button>
        <button type="button" className="btn-secundario" onClick={onCancelar} disabled={enviando}>
          Cancelar
        </button>
      </div>
    </form>
  );
}
