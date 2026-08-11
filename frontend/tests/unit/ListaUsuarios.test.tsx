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

  it('edita o nome e o papel de um usuário', async () => {
    vi.spyOn(api, 'listarUsuarios').mockResolvedValue([
      { id: 1, nome: 'Ana', username: 'ana', papel: 'VISUALIZADOR', ativo: true },
    ]);
    const alterar = vi
      .spyOn(api, 'alterarUsuario')
      .mockResolvedValue({ id: 1, nome: 'Ana Paula', username: 'ana', papel: 'SECRETARIA', ativo: true });

    const user = userEvent.setup();
    render(<ListaUsuarios />);
    await screen.findByText('ana');

    await user.click(screen.getByRole('button', { name: /editar/i }));
    const form = screen.getByRole('form', { name: /editar usuário/i });
    const nome = within(form).getByLabelText('Nome');
    await user.clear(nome);
    await user.type(nome, 'Ana Paula');
    await user.selectOptions(within(form).getByLabelText('Papel'), 'SECRETARIA');
    await user.click(within(form).getByRole('button', { name: /salvar alterações/i }));

    expect(alterar).toHaveBeenCalledWith(1, { nome: 'Ana Paula', papel: 'SECRETARIA' });
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
