import { AuthNav } from '@/features/auth/ui/AuthNav';
import { RequireAuth } from '@/features/auth/ui/RequireAuth';
import { ListaRegistros } from '@/features/busca-registros/ui/ListaRegistros';

export default function RegistrosPage() {
  return (
    <RequireAuth>
      <main>
        <AuthNav />
        <div>
          <h1>Registros de matrícula</h1>
          <p>Busque um aluno pelo nome ou RM e gerencie o índice Nome → RM.</p>
        </div>
        <ListaRegistros />
      </main>
    </RequireAuth>
  );
}
