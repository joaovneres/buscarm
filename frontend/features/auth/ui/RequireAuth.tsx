'use client';

import { usePathname, useRouter } from 'next/navigation';
import { useEffect, type ReactNode } from 'react';

import { useAuth } from '../model/AuthContext';

/**
 * Protege uma rota: exige sessão e, se a senha for provisória (após reset do admin),
 * força a troca antes de liberar o resto do sistema.
 */
export function RequireAuth({ children }: { children: ReactNode }) {
  const { autenticado, carregando, usuario } = useAuth();
  const router = useRouter();
  const pathname = usePathname();
  const precisaTrocarSenha = !!usuario?.senhaProvisoria && pathname !== '/trocar-senha';

  useEffect(() => {
    if (carregando) {
      return;
    }
    if (!autenticado) {
      router.replace('/login');
    } else if (precisaTrocarSenha) {
      router.replace('/trocar-senha');
    }
  }, [carregando, autenticado, precisaTrocarSenha, router]);

  if (carregando || !autenticado || precisaTrocarSenha) {
    return <p role="status">Carregando…</p>;
  }
  return <>{children}</>;
}
