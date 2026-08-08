'use client';

import { useRouter } from 'next/navigation';
import { useState, type FormEvent } from 'react';

import { useAuth } from '../model/AuthContext';

export function LoginForm() {
  const { entrar } = useAuth();
  const router = useRouter();
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [erro, setErro] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setEnviando(true);
    setErro(null);
    try {
      await entrar(email.trim(), senha);
      router.push('/registros');
    } catch (err) {
      setErro((err as { message?: string })?.message ?? 'Não foi possível entrar');
      setEnviando(false);
    }
  }

  return (
    <form onSubmit={onSubmit} aria-label="Entrar">
      <label htmlFor="email">E-mail</label>
      <input
        id="email"
        name="email"
        type="email"
        autoComplete="username"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
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
