import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';

import { TabelaRegistros } from '@/features/busca-registros/ui/TabelaRegistros';
import type { RegistroResponse } from '@/entities/registro/model/types';

function registro(over: Partial<RegistroResponse>): RegistroResponse {
  return {
    id: 1,
    rm: 100,
    nomeCompleto: 'Alves, Ana',
    sobrenome: 'Alves',
    anoRegistro: null,
    ativo: true,
    status: 'VALIDADO',
    criadoPor: 'x',
    atualizadoPor: 'x',
    criadoEm: '2026-01-01T00:00:00Z',
    atualizadoEm: null,
    ...over,
  };
}

const noop = () => {};

describe('TabelaRegistros', () => {
  it('mostra divisórias por letra e todas as ações para quem escreve', () => {
    render(
      <TabelaRegistros
        papel="ADMIN"
        registros={[
          registro({ id: 1, sobrenome: 'Alves', nomeCompleto: 'Alves, Ana' }),
          registro({ id: 2, sobrenome: 'Bruno', nomeCompleto: 'Bruno, Caio' }),
        ]}
        onDetalhe={noop}
        onEditar={noop}
        onExcluir={noop}
      />,
    );
    expect(screen.getByText('A', { selector: 'td' })).toBeInTheDocument();
    expect(screen.getByText('B', { selector: 'td' })).toBeInTheDocument();
    expect(screen.getAllByRole('button', { name: /^editar/i })).toHaveLength(2);
    expect(screen.getAllByRole('button', { name: /^excluir/i })).toHaveLength(2);
  });

  it('esconde editar/excluir para o visualizador', () => {
    render(
      <TabelaRegistros
        papel="VISUALIZADOR"
        registros={[registro({ id: 1 })]}
        onDetalhe={noop}
        onEditar={noop}
        onExcluir={noop}
      />,
    );
    expect(screen.queryByRole('button', { name: /^editar/i })).toBeNull();
    expect(screen.queryByRole('button', { name: /^excluir/i })).toBeNull();
    expect(screen.getByRole('button', { name: /ver detalhes/i })).toBeInTheDocument();
  });

  it('dispara os callbacks das ações', async () => {
    const onEditar = vi.fn();
    const user = userEvent.setup();
    render(
      <TabelaRegistros
        papel="SECRETARIA"
        registros={[registro({ id: 7, nomeCompleto: 'Alves, Ana' })]}
        onDetalhe={noop}
        onEditar={onEditar}
        onExcluir={noop}
      />,
    );
    await user.click(screen.getByRole('button', { name: /editar alves, ana/i }));
    expect(onEditar).toHaveBeenCalledWith(expect.objectContaining({ id: 7 }));
  });
});
