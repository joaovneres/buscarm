import { BuscaRegistros } from '@/features/registros/components/BuscaRegistros';

export default function RegistrosPage() {
  return (
    <main>
      <h1>Buscar registro de matrícula</h1>
      <p>Pesquise pelo nome (sobrenome, nome) ou diretamente pelo RM.</p>
      <BuscaRegistros />
    </main>
  );
}
