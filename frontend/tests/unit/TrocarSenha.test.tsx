import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { TrocarSenha } from '@/features/conta/ui/TrocarSenha';
import * as contaApi from '@/entities/usuario/api/conta';

const marcarSenhaTrocada = vi.fn();

vi.mock('next/navigation', () => ({ useRouter: () => ({ push: vi.fn() }) }));
vi.mock('@/features/auth/model/AuthContext', () => ({
  useAuth: () => ({ usuario: { senhaProvisoria: true }, marcarSenhaTrocada }),
}));

describe('TrocarSenha', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
    marcarSenhaTrocada.mockClear();
  });

  it('avisa quando a confirmação não confere e não chama a API', async () => {
    const spy = vi.spyOn(contaApi, 'trocarSenha').mockResolvedValue();
    const user = userEvent.setup();
    render(<TrocarSenha />);

    await user.type(screen.getByLabelText('Senha atual'), 'atual123');
    await user.type(screen.getByLabelText('Nova senha (mín. 8 caracteres)'), 'novaSenha1');
    await user.type(screen.getByLabelText('Confirmar nova senha'), 'diferente1');
    await user.click(screen.getByRole('button', { name: /trocar senha/i }));

    expect(await screen.findByRole('alert')).toHaveTextContent(/confer/i);
    expect(spy).not.toHaveBeenCalled();
  });

  it('troca a senha e limpa o flag de provisória', async () => {
    const spy = vi.spyOn(contaApi, 'trocarSenha').mockResolvedValue();
    const user = userEvent.setup();
    render(<TrocarSenha />);

    await user.type(screen.getByLabelText('Senha atual'), 'temp12345');
    await user.type(screen.getByLabelText('Nova senha (mín. 8 caracteres)'), 'novaSenha1');
    await user.type(screen.getByLabelText('Confirmar nova senha'), 'novaSenha1');
    await user.click(screen.getByRole('button', { name: /trocar senha/i }));

    expect(spy).toHaveBeenCalledWith('temp12345', 'novaSenha1');
    expect(marcarSenhaTrocada).toHaveBeenCalled();
  });
});
