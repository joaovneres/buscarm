'use client';

import { useRouter } from 'next/navigation';
import { useState, type FormEvent } from 'react';

import { trocarSenha } from '@/entities/usuario/api/conta';
import { useAuth } from '@/features/auth/model/AuthContext';

export function TrocarSenha() {
  const { usuario, marcarSenhaTrocada } = useAuth();
  const router = useRouter();
  const [atual, setAtual] = useState('');
  const [nova, setNova] = useState('');
  const [confirma, setConfirma] = useState('');
  const [erro, setErro] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);
  const [ok, setOk] = useState(false);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    if (nova.length < 8) {
      setErro('A nova senha deve ter ao menos 8 caracteres.');
      return;
    }
    if (nova !== confirma) {
      setErro('A confirmação não confere com a nova senha.');
      return;
    }
    setEnviando(true);
    setErro(null);
    try {
      await trocarSenha(atual, nova);
      marcarSenhaTrocada();
      setOk(true);
      setTimeout(() => router.push('/registros'), 900);
    } catch (err) {
      setErro((err as { message?: string })?.message ?? 'Não foi possível trocar a senha');
      setEnviando(false);
    }
  }

  return (
    <form onSubmit={onSubmit} aria-label="Trocar senha">
      {usuario?.senhaProvisoria && (
        <p role="status">
          Sua senha foi redefinida pelo administrador. Defina uma nova para continuar.
        </p>
      )}

      <label htmlFor="senha-atual">Senha atual</label>
      <input
        id="senha-atual"
        type="password"
        autoComplete="current-password"
        value={atual}
        onChange={(e) => setAtual(e.target.value)}
        required
      />

      <label htmlFor="senha-nova">Nova senha (mín. 8 caracteres)</label>
      <input
        id="senha-nova"
        type="password"
        autoComplete="new-password"
        value={nova}
        onChange={(e) => setNova(e.target.value)}
        required
      />

      <label htmlFor="senha-confirma">Confirmar nova senha</label>
      <input
        id="senha-confirma"
        type="password"
        autoComplete="new-password"
        value={confirma}
        onChange={(e) => setConfirma(e.target.value)}
        required
      />

      {erro && <p role="alert">{erro}</p>}
      {ok && <p role="status">Senha alterada com sucesso. Redirecionando…</p>}

      <button type="submit" disabled={enviando || ok}>
        {enviando ? 'Salvando…' : 'Trocar senha'}
      </button>
    </form>
  );
}
