import { AuthNav } from '@/features/auth/ui/AuthNav';
import { RequireAuth } from '@/features/auth/ui/RequireAuth';
import { ConsultarAuditoria } from '@/features/consultar-auditoria/ui/ConsultarAuditoria';

export default function AuditoriaPage() {
  return (
    <RequireAuth>
      <main>
        <AuthNav />
        <h1>Auditoria</h1>
        <p>Histórico das ações sensíveis (acesso restrito a administradores).</p>
        <ConsultarAuditoria />
      </main>
    </RequireAuth>
  );
}
