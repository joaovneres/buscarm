import type { Metadata } from 'next';

import './globals.css';

export const metadata: Metadata = {
  title: 'BuscaRM',
  description: 'Índice pesquisável Nome → RM do livro de matrícula',
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="pt-BR">
      <body>{children}</body>
    </html>
  );
}
