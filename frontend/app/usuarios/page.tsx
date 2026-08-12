'use client';

import Link from 'next/link';

import { useAuth } from '@/features/auth/model/AuthContext';
import { AuthNav } from '@/features/auth/ui/AuthNav';
import { RequireAuth } from '@/features/auth/ui/RequireAuth';
import { ListaUsuarios } from '@/features/gestao-usuarios/ui/ListaUsuarios';
import { ehAdmin } from '@/shared/auth/roles';

export default function UsuariosPage() {
  const { usuario } = useAuth();

  return (
    <RequireAuth>
      <AuthNav />
      <main>
        <div>
          <h1>Usuários</h1>
          <p>Cadastre quem acessa o sistema e redefina senhas quando necessário.</p>
        </div>
        {ehAdmin(usuario?.papel) ? (
          <ListaUsuarios />
        ) : (
          <section>
            <p>Acesso restrito a administradores.</p>
            <Link href="/registros">Voltar aos registros</Link>
          </section>
        )}
      </main>
    </RequireAuth>
  );
}
