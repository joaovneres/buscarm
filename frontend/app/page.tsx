'use client';

import { useRouter } from 'next/navigation';
import { useEffect } from 'react';

import { TelaLogin } from '@/features/auth/ui/TelaLogin';
import { useAuth } from '@/features/auth/model/AuthContext';

export default function Home() {
  const { autenticado, carregando } = useAuth();
  const router = useRouter();

  // Não há tela inicial própria: logado vai direto para os registros.
  useEffect(() => {
    if (!carregando && autenticado) {
      router.replace('/registros');
    }
  }, [carregando, autenticado, router]);

  if (carregando) {
    return (
      <div className="tela-espera">
        <p role="status">Carregando…</p>
      </div>
    );
  }

  if (autenticado) {
    return (
      <div className="tela-espera">
        <p role="status">Redirecionando…</p>
      </div>
    );
  }

  // Deslogado: a home É a tela de login.
  return <TelaLogin />;
}
