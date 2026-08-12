'use client';

import { AuthNav } from '@/features/auth/ui/AuthNav';
import { TelaLogin } from '@/features/auth/ui/TelaLogin';
import { useAuth } from '@/features/auth/model/AuthContext';
import { HomeNav } from '@/features/navegacao/ui/HomeNav';

export default function Home() {
  const { autenticado, carregando } = useAuth();

  if (carregando) {
    return (
      <div className="tela-espera">
        <p role="status">Carregando…</p>
      </div>
    );
  }

  // Deslogado: a home É a tela de login.
  if (!autenticado) {
    return <TelaLogin />;
  }

  // Logado: hub do app com a navegação disponível.
  return (
    <>
      <AuthNav />
      <main>
        <div>
          <h1>BuscaRM</h1>
          <p>Encontre o RM de um aluno pelo nome para localizar o prontuário físico.</p>
        </div>
        <HomeNav />
      </main>
    </>
  );
}
