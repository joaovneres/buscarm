import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';

import { TabelaAuditoria } from '@/features/consultar-auditoria/ui/TabelaAuditoria';

describe('TabelaAuditoria', () => {
  it('lista os eventos de auditoria', () => {
    render(
      <TabelaAuditoria
        itens={[
          {
            id: 1,
            usuario: 'secretaria',
            acao: 'CRIAR',
            entidade: 'REGISTRO',
            entidadeId: 42,
            detalhe: 'RM 203',
            criadoEm: '2026-08-08T12:00:00Z',
          },
        ]}
      />,
    );

    expect(screen.getByText('CRIAR')).toBeInTheDocument();
    expect(screen.getByText('secretaria')).toBeInTheDocument();
    expect(screen.getByText('RM 203')).toBeInTheDocument();
  });

  it('mostra vazio quando não há eventos', () => {
    render(<TabelaAuditoria itens={[]} />);
    expect(screen.getByText(/nenhum evento/i)).toBeInTheDocument();
  });
});
