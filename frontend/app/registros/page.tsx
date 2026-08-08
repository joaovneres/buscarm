import { BuscaRegistros } from '@/features/busca-registros/ui/BuscaRegistros';
import { ExportarRegistros } from '@/features/exportar-registros/ui/ExportarRegistros';

export default function RegistrosPage() {
  return (
    <main>
      <h1>Buscar registro de matrícula</h1>
      <p>Pesquise pelo nome (sobrenome, nome) ou diretamente pelo RM.</p>
      <BuscaRegistros />
      <ExportarRegistros />
    </main>
  );
}
