import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { ListaRegistros } from '@/features/busca-registros/ui/ListaRegistros';
import * as buscaApi from '@/entities/registro/api/buscarRegistros';
import * as cadApi from '@/entities/registro/api/cadastrarRegistro';
import type { RegistroResponse } from '@/entities/registro/model/types';

vi.mock('@/features/auth/model/AuthContext', () => ({
  useAuth: () => ({
    usuario: { papel: 'ADMIN', nome: 'Admin', username: 'admin' },
    autenticado: true,
    carregando: false,
  }),
}));

function registro(over: Partial<RegistroResponse>): RegistroResponse {
  return {
    id: 1,
    rm: 203,
    nomeCompleto: 'Sousa, Ana',
    sobrenome: 'Sousa',
    anoRegistro: null,
    ativo: true,
    status: 'VALIDADO',
    criadoPor: 'sec',
    atualizadoPor: 'sec',
    criadoEm: '2026-01-01T00:00:00Z',
    atualizadoEm: null,
    ...over,
  };
}

function pagina(conteudo: RegistroResponse[]) {
  return { conteudo, pagina: 0, tamanho: 20, total: conteudo.length, totalPaginas: 1 };
}

describe('ListaRegistros', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('carrega a lista, abre o modal de novo registro e cadastra recarregando', async () => {
    const buscar = vi
      .spyOn(buscaApi, 'buscarRegistros')
      .mockResolvedValue(pagina([registro({ id: 1, nomeCompleto: 'Sousa, Ana', rm: 203 })]));
    const cadastrar = vi
      .spyOn(cadApi, 'cadastrarRegistro')
      .mockResolvedValue(registro({ id: 2, nomeCompleto: 'Lima, Rui', rm: 300 }));

    const user = userEvent.setup();
    render(<ListaRegistros />);

    expect(await screen.findByText('Sousa, Ana')).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: /novo registro/i }));
    await user.type(screen.getByLabelText('RM'), '300');
    await user.type(screen.getByLabelText('Nome completo'), 'Lima, Rui');
    await user.click(screen.getByRole('button', { name: /^cadastrar$/i }));

    expect(cadastrar).toHaveBeenCalledWith({ rm: 300, nomeCompleto: 'Lima, Rui', anoRegistro: null });
    await waitFor(() => expect(buscar).toHaveBeenCalledTimes(2)); // carga inicial + recarga
  });

  it('busca por RM quando o termo é numérico', async () => {
    const buscar = vi
      .spyOn(buscaApi, 'buscarRegistros')
      .mockResolvedValue(pagina([registro({ id: 1, rm: 203 })]));

    const user = userEvent.setup();
    render(<ListaRegistros />);
    await screen.findByText('Sousa, Ana');

    await user.type(screen.getByLabelText(/nome ou rm/i), '203');
    await user.click(screen.getByRole('button', { name: /^buscar$/i }));

    await waitFor(() =>
      expect(buscar).toHaveBeenLastCalledWith(expect.objectContaining({ rm: '203' })),
    );
  });
});
