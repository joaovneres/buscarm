import { AuthNav } from '@/features/auth/ui/AuthNav';
import { RequireAuth } from '@/features/auth/ui/RequireAuth';
import { ImportarImagem } from '@/features/importar-imagem/ui/ImportarImagem';

export default function ImportacaoImagemPage() {
  return (
    <RequireAuth>
      <AuthNav />
      <main>
        <h1>Importar por foto (OCR)</h1>
        <p>
          Fotografe uma página do livro. O sistema reconhece o texto; você <strong>revisa e
          corrige</strong> antes de gravar. Nada é salvo sem sua confirmação.
        </p>
        <ImportarImagem />
      </main>
    </RequireAuth>
  );
}
