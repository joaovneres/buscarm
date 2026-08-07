import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { BuscaRegistros } from '@/features/registros/components/BuscaRegistros';
import * as api from '@/features/registros/api';

describe('BuscaRegistros', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('busca por nome e destaca o RM no resultado', async () => {
    vi.spyOn(api, 'buscarRegistros').mockResolvedValue({
      conteudo: [
        {
          id: 1,
          rm: 203,
          nomeCompleto: 'Sousa, João Victor Cosme Neres de',
          sobrenome: 'Sousa',
          anoRegistro: null,
          ativo: true,
          criadoPor: 'sistema',
          atualizadoPor: 'sistema',
          criadoEm: '2026-01-01T00:00:00Z',
        },
      ],
      pagina: 0,
      tamanho: 20,
      total: 1,
      totalPaginas: 1,
    });

    const user = userEvent.setup();
    render(<BuscaRegistros />);

    await user.type(screen.getByRole('textbox'), 'joao');
    await user.click(screen.getByRole('button', { name: /buscar/i }));

    expect(await screen.findByText('203')).toBeInTheDocument();
    expect(screen.getByText('Sousa, João Victor Cosme Neres de')).toBeInTheDocument();
    expect(api.buscarRegistros).toHaveBeenCalledWith({ nome: 'joao' });
  });

  it('mostra mensagem de erro quando a API falha', async () => {
    vi.spyOn(api, 'buscarRegistros').mockRejectedValue({ message: 'API indisponível' });

    const user = userEvent.setup();
    render(<BuscaRegistros />);

    await user.type(screen.getByRole('textbox'), 'lara');
    await user.click(screen.getByRole('button', { name: /buscar/i }));

    expect(await screen.findByRole('alert')).toHaveTextContent('API indisponível');
  });
});
