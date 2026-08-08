import type { Metadata } from 'next';

import { AuthProvider } from '@/features/auth/model/AuthContext';

import './globals.css';

export const metadata: Metadata = {
  title: 'BuscaRM',
  description: 'Índice pesquisável Nome → RM do livro de matrícula',
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="pt-BR">
      <body>
        <AuthProvider>{children}</AuthProvider>
      </body>
    </html>
  );
}
