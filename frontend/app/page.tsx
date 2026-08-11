import { AuthNav } from '@/features/auth/ui/AuthNav';
import { HomeNav } from '@/features/navegacao/ui/HomeNav';

export default function Home() {
  return (
    <main>
      <AuthNav />
      <div>
        <h1>BuscaRM</h1>
        <p>Encontre o RM de um aluno pelo nome para localizar o prontuário físico.</p>
      </div>
      <HomeNav />
    </main>
  );
}
