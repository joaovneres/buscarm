import { AuthNav } from '@/features/auth/ui/AuthNav';
import { RequireAuth } from '@/features/auth/ui/RequireAuth';
import { BuscaRegistros } from '@/features/busca-registros/ui/BuscaRegistros';
import { CadastrarRegistro } from '@/features/cadastrar-registro/ui/CadastrarRegistro';
import { ExportarRegistros } from '@/features/exportar-registros/ui/ExportarRegistros';

export default function RegistrosPage() {
  return (
    <RequireAuth>
      <main>
        <AuthNav />
        <h1>Buscar registro de matrícula</h1>
        <p>Pesquise pelo nome (sobrenome, nome) ou diretamente pelo RM.</p>
        <CadastrarRegistro />
        <BuscaRegistros />
        <ExportarRegistros />
      </main>
    </RequireAuth>
  );
}
