import { LoginForm } from '@/features/auth/ui/LoginForm';
import { BotaoTema } from '@/features/tema/ui/BotaoTema';

export default function LoginPage() {
  return (
    <main>
      <div className="login-topo">
        <BotaoTema />
      </div>
      <h1>Entrar no BuscaRM</h1>
      <p>Acesse com suas credenciais da secretaria.</p>
      <LoginForm />
    </main>
  );
}
