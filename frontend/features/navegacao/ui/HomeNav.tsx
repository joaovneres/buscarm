'use client';

import Link from 'next/link';

import { useAuth } from '@/features/auth/model/AuthContext';
import { itensVisiveis } from '@/shared/auth/roles';

/** Menu da área logada; os itens variam conforme o papel do usuário. */
export function HomeNav() {
  const { usuario, autenticado, carregando } = useAuth();

  if (carregando) {
    return <p role="status">Carregando…</p>;
  }

  if (!autenticado || !usuario) {
    return (
      <p>
        <Link href="/login">Entrar</Link> para acessar o sistema.
      </p>
    );
  }

  const itens = itensVisiveis(usuario.papel);

  return (
    <nav aria-label="Ações">
      <ul>
        {itens.map((item) => (
          <li key={item.href}>
            <Link href={item.href}>
              <span className="cartao-nav__titulo">{item.titulo}</span>
              <span className="cartao-nav__desc">{item.descricao}</span>
            </Link>
          </li>
        ))}
      </ul>
    </nav>
  );
}
