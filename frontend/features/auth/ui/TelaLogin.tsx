'use client';

import { BotaoTema } from '@/features/tema/ui/BotaoTema';

import { LoginForm } from './LoginForm';

/**
 * Tela de entrada (home deslogada e rota /login): split-screen com o painel
 * de marca à esquerda — o "fichário" do índice físico — e o formulário
 * centralizado à direita. No mobile, só o formulário (com a marca no topo).
 */
export function TelaLogin() {
  return (
    <div className="auth">
      <div className="auth__topo">
        <BotaoTema />
      </div>

      {/* Painel de marca — puramente decorativo. */}
      <aside className="auth__hero" aria-hidden="true">
        <div className="auth__marca">
          <span className="auth__stamp">RM</span>
          <span className="auth__wordmark">BuscaRM</span>
        </div>

        <p className="auth__lema">O índice do livro de matrículas, agora pesquisável.</p>

        <div className="fichario">
          <article className="ficha ficha--c">
            <span className="ficha__tab">C</span>
            <span className="ficha__nome" />
            <span className="ficha__rm">RM 004821</span>
          </article>
          <article className="ficha ficha--b">
            <span className="ficha__tab">B</span>
            <span className="ficha__nome" />
            <span className="ficha__rm">RM 002140</span>
          </article>
          <article className="ficha ficha--a">
            <span className="ficha__tab">A</span>
            <span className="ficha__nome" />
            <span className="ficha__rm">RM 000317</span>
          </article>
        </div>

        <p className="auth__rodape">Uso interno · Secretaria escolar</p>
      </aside>

      {/* Painel do formulário. */}
      <main className="auth__painel">
        <div className="auth__cartao">
          <span className="auth__cartao-marca">BuscaRM</span>
          <h1>Entrar no BuscaRM</h1>
          <p>Acesse com suas credenciais da secretaria.</p>
          <LoginForm />
        </div>
      </main>
    </div>
  );
}
