import Link from 'next/link';

/**
 * Atalhos de importação em massa (planilha ou foto), exibidos no topo da lista, ao lado do
 * Exportar. São navegação — por isso links — para os fluxos dedicados de importação.
 */
export function ImportarRegistros() {
  return (
    <section aria-label="Importar registros">
      <h2>Importar</h2>
      <p>Carregue vários alunos de uma planilha (.xlsx/.csv) ou foto.</p>
      <Link className="btn" href="/importacao/planilha">
        Importar planilha
      </Link>{' '}
      <Link className="btn" href="/importacao/imagem">
        Importar por foto
      </Link>
    </section>
  );
}
