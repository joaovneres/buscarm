import { AuthNav } from '@/features/auth/ui/AuthNav';
import { RequireAuth } from '@/features/auth/ui/RequireAuth';
import { ImportarPlanilha } from '@/features/importar-planilha/ui/ImportarPlanilha';

export default function ImportacaoPlanilhaPage() {
  return (
    <RequireAuth>
      <AuthNav />
      <main>
        <h1>Importar registros por planilha</h1>
        <p>
          Envie a planilha em <strong>.xlsx</strong> ou <strong>.csv</strong> com as colunas{' '}
          <strong>Nome</strong> e <strong>RM</strong>. Nada é gravado sem sua confirmação na
          pré-visualização.
        </p>
        <ImportarPlanilha />
      </main>
    </RequireAuth>
  );
}
