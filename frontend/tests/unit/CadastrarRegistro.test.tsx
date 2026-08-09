import { render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { CadastrarRegistro } from '@/features/cadastrar-registro/ui/CadastrarRegistro';
import * as api from '@/entities/registro/api/cadastrarRegistro';
import type { RegistroResponse } from '@/entities/registro/model/types';

function registro(over: Partial<RegistroResponse> = {}): RegistroResponse {
  return {
    id: 1,
    rm: 815,
    nomeCompleto: 'Nunes, Marina',
    sobrenome: 'Nunes',
    anoRegistro: 2021,
    ativo: true,
    criadoPor: 'secretaria',
    atualizadoPor: 'secretaria',
    criadoEm: '2026-01-01T00:00:00Z',
    ...over,
  };
}

describe('CadastrarRegistro', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  async function abrirFormulario(user: ReturnType<typeof userEvent.setup>) {
    await user.click(screen.getByRole('button', { name: /novo registro/i }));
    return screen.getByRole('form', { name: /cadastrar registro/i });
  }

  it('cadastra um registro e mostra o RM criado, limpando os campos', async () => {
    const spy = vi.spyOn(api, 'cadastrarRegistro').mockResolvedValue(registro());

    const user = userEvent.setup();
    render(<CadastrarRegistro />);
    const form = await abrirFormulario(user);

    await user.type(screen.getByLabelText('RM'), '815');
    await user.type(screen.getByLabelText(/nome \(sobrenome/i), 'Nunes, Marina');
    await user.type(screen.getByLabelText(/ano/i), '2021');
    await user.click(screen.getByRole('button', { name: /cadastrar/i }));

    expect(spy).toHaveBeenCalledWith({
      rm: 815,
      nomeCompleto: 'Nunes, Marina',
      anoRegistro: 2021,
    });
    expect(await screen.findByRole('status')).toHaveTextContent('RM 815');
    // campos limpos após sucesso
    expect(screen.getByLabelText('RM')).toHaveValue('');
    expect(within(form).queryByDisplayValue('Nunes, Marina')).not.toBeInTheDocument();
  });

  it('envia anoRegistro null quando o ano fica em branco', async () => {
    const spy = vi.spyOn(api, 'cadastrarRegistro').mockResolvedValue(registro({ anoRegistro: null }));

    const user = userEvent.setup();
    render(<CadastrarRegistro />);
    await abrirFormulario(user);

    await user.type(screen.getByLabelText('RM'), '900');
    await user.type(screen.getByLabelText(/nome \(sobrenome/i), 'Rocha, Ian');
    await user.click(screen.getByRole('button', { name: /cadastrar/i }));

    expect(spy).toHaveBeenCalledWith({
      rm: 900,
      nomeCompleto: 'Rocha, Ian',
      anoRegistro: null,
    });
  });

  it('mostra erro do backend quando o RM já existe', async () => {
    vi.spyOn(api, 'cadastrarRegistro').mockRejectedValue({ message: 'RM 815 já cadastrado' });

    const user = userEvent.setup();
    render(<CadastrarRegistro />);
    await abrirFormulario(user);

    await user.type(screen.getByLabelText('RM'), '815');
    await user.type(screen.getByLabelText(/nome \(sobrenome/i), 'Nunes, Marina');
    await user.click(screen.getByRole('button', { name: /cadastrar/i }));

    expect(await screen.findByRole('alert')).toHaveTextContent('RM 815 já cadastrado');
  });
});
