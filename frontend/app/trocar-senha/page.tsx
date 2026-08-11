import { AuthNav } from '@/features/auth/ui/AuthNav';
import { RequireAuth } from '@/features/auth/ui/RequireAuth';
import { TrocarSenha } from '@/features/conta/ui/TrocarSenha';

export default function TrocarSenhaPage() {
  return (
    <RequireAuth>
      <main>
        <AuthNav />
        <div>
          <h1>Trocar senha</h1>
          <p>Escolha uma nova senha para a sua conta.</p>
        </div>
        <TrocarSenha />
      </main>
    </RequireAuth>
  );
}
