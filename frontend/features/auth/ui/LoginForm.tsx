'use client';

import { useRouter } from 'next/navigation';
import { useState, type FormEvent } from 'react';

import { useAuth } from '../model/AuthContext';

export function LoginForm() {
  const { entrar } = useAuth();
  const router = useRouter();
  const [username, setUsername] = useState('');
  const [senha, setSenha] = useState('');
  const [erro, setErro] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setEnviando(true);
    setErro(null);
    try {
      const u = await entrar(username.trim(), senha);
      router.push(u.senhaProvisoria ? '/trocar-senha' : '/registros');
    } catch (err) {
      setErro((err as { message?: string })?.message ?? 'Não foi possível entrar');
      setEnviando(false);
    }
  }

  return (
    <form onSubmit={onSubmit} aria-label="Entrar">
      <label htmlFor="username">Usuário</label>
      <input
        id="username"
        name="username"
        type="text"
        autoComplete="username"
        value={username}
        onChange={(e) => setUsername(e.target.value)}
        required
      />

      <label htmlFor="senha">Senha</label>
      <input
        id="senha"
        name="senha"
        type="password"
        autoComplete="current-password"
        value={senha}
        onChange={(e) => setSenha(e.target.value)}
        required
      />

      {erro && <p role="alert">{erro}</p>}

      <button type="submit" disabled={enviando}>
        {enviando ? 'Entrando…' : 'Entrar'}
      </button>
    </form>
  );
}
