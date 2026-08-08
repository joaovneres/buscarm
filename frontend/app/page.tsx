import Link from 'next/link';

export default function Home() {
  return (
    <main>
      <h1>BuscaRM</h1>
      <p>Encontre o número de RM de um aluno pelo nome, para localizar o prontuário físico.</p>
      <nav aria-label="Ações">
        <ul>
          <li>
            <Link href="/registros">Buscar registros →</Link>
          </li>
          <li>
            <Link href="/importacao/planilha">Importar planilha →</Link>
          </li>
        </ul>
      </nav>
    </main>
  );
}
