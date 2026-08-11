import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { ListaUsuarios } from '@/features/gestao-usuarios/ui/ListaUsuarios';
import * as api from '@/entities/usuario/api/usuarios';

describe('ListaUsuarios', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('lista os usuários e cadastra um novo recarregando', async () => {
    const listar = vi
      .spyOn(api, 'listarUsuarios')
      .mockResolvedValue([{ id: 1, nome: 'Ana', username: 'ana', papel: 'VISUALIZADOR', ativo: true }]);
    const criar = vi
      .spyOn(api, 'criarUsuario')
      .mockResolvedValue({ id: 2, nome: 'Rui', username: 'rui', papel: 'ADMIN', ativo: true });

    const user = userEvent.setup();
    render(<ListaUsuarios />);

    expect(await screen.findByText('ana')).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: /novo usuário/i }));
    await user.type(screen.getByLabelText('Nome'), 'Rui');
    await user.type(screen.getByLabelText(/usuário \(para login/i), 'rui');
    await user.type(screen.getByLabelText(/senha provisória/i), 'segredo123');
    await user.click(screen.getByRole('button', { name: /cadastrar usuário/i }));

    expect(criar).toHaveBeenCalledWith({
      nome: 'Rui',
      username: 'rui',
      senha: 'segredo123',
      papel: 'VISUALIZADOR',
    });
    await waitFor(() => expect(listar).toHaveBeenCalledTimes(2));
  });

  it('redefine a senha de um usuário', async () => {
    vi.spyOn(api, 'listarUsuarios').mockResolvedValue([
      { id: 1, nome: 'Ana', username: 'ana', papel: 'VISUALIZADOR', ativo: true },
    ]);
    const reset = vi.spyOn(api, 'resetarSenha').mockResolvedValue();

    const user = userEvent.setup();
    render(<ListaUsuarios />);
    await screen.findByText('ana');

    await user.click(screen.getByRole('button', { name: /redefinir senha/i }));
    const form = screen.getByRole('form', { name: /redefinir senha/i });
    await user.type(within(form).getByLabelText(/nova senha provisória/i), 'temp12345');
    await user.click(within(form).getByRole('button', { name: /redefinir/i }));

    expect(reset).toHaveBeenCalledWith(1, 'temp12345');
  });
});
