import Link from 'next/link';

export default function Home() {
  return (
    <main>
      <h1>BuscaRM</h1>
      <p>Encontre o número de RM de um aluno pelo nome, para localizar o prontuário físico.</p>
      <Link href="/registros">Buscar registros →</Link>
    </main>
  );
}
