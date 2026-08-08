'use client';

import Link from 'next/link';
import { useRouter } from 'next/navigation';

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
    return <Link href="/login">Entrar</Link>;
  }

  return (
    <p>
      {usuario.nome} ({usuario.papel}){' '}
      {usuario.papel === 'ADMIN' && (
        <>
          <Link href="/auditoria">Auditoria</Link>{' '}
        </>
      )}
      <button type="button" onClick={onSair}>
        Sair
      </button>
    </p>
  );
}
