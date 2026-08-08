'use client';

import { useRouter } from 'next/navigation';
import { useEffect, type ReactNode } from 'react';

import { useAuth } from '../model/AuthContext';

/** Protege uma rota: redireciona para /login se não houver sessão. */
export function RequireAuth({ children }: { children: ReactNode }) {
  const { autenticado, carregando } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (!carregando && !autenticado) {
      router.replace('/login');
    }
  }, [carregando, autenticado, router]);

  if (carregando || !autenticado) {
    return <p role="status">Carregando…</p>;
  }
  return <>{children}</>;
}
