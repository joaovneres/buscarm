'use client';

import Link from 'next/link';
import { useRouter } from 'next/navigation';

import { rotuloPapel } from '@/shared/auth/roles';

import { useAuth } from '../model/AuthContext';

/** Mostra o usuário logado e o botão sair, ou um link para entrar. */
export function AuthNav() {
  const { usuario, autenticado, sair } = useAuth();
  const router = useRouter();

  function onSair() {
    sair();
    router.push('/login');
  }

  if (!autenticado || !usuario) {
    return (
      <header className="appbar">
        <Link href="/" className="appbar__brand">
          BuscaRM
        </Link>
        <div className="appbar__right">
          <Link href="/login">Entrar</Link>
        </div>
      </header>
    );
  }

  return (
    <header className="appbar">
      <Link href="/registros" className="appbar__brand">
        BuscaRM
      </Link>
      <div className="appbar__right">
        <span className="appbar__user">
          {usuario.nome}
          <span className="appbar__role">{rotuloPapel(usuario.papel)}</span>
        </span>
        {usuario.papel === 'ADMIN' && <Link href="/auditoria">Auditoria</Link>}
        <button type="button" onClick={onSair}>
          Sair
        </button>
      </div>
    </header>
  );
}
