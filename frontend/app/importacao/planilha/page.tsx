import { AuthNav } from '@/features/auth/ui/AuthNav';
import { RequireAuth } from '@/features/auth/ui/RequireAuth';
import { ImportarPlanilha } from '@/features/importar-planilha/ui/ImportarPlanilha';

export default function ImportacaoPlanilhaPage() {
  return (
    <RequireAuth>
      <main>
        <AuthNav />
        <h1>Importar registros por planilha</h1>
        <p>
          Envie a planilha (.xlsx) com as colunas <strong>Nome</strong> e <strong>RM</strong>. Nada é
          gravado sem sua confirmação na pré-visualização.
        </p>
        <ImportarPlanilha />
      </main>
    </RequireAuth>
  );
}
