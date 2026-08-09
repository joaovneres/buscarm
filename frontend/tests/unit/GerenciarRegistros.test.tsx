import { render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import { BuscaRegistros } from '@/features/busca-registros/ui/BuscaRegistros';
import * as buscaApi from '@/entities/registro/api/buscarRegistros';
import * as gerenciarApi from '@/entities/registro/api/alterarRegistro';
import type { RegistroResponse } from '@/entities/registro/model/types';

function registro(over: Partial<RegistroResponse> = {}): RegistroResponse {
  return {
    id: 1,
    rm: 203,
    nomeCompleto: 'Sousa, João',
    sobrenome: 'Sousa',
    anoRegistro: null,
    ativo: true,
    criadoPor: 'secretaria',
    atualizadoPor: 'secretaria',
    criadoEm: '2026-01-01T00:00:00Z',
    ...over,
  };
}

function pagina(conteudo: RegistroResponse[]) {
  return { conteudo, pagina: 0, tamanho: 20, total: conteudo.length, totalPaginas: 1 };
}

describe('Gerenciar registros (alterar/desativar)', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  async function buscar(user: ReturnType<typeof userEvent.setup>) {
    await user.type(screen.getByRole('textbox'), 'joao');
    await user.click(screen.getByRole('button', { name: /buscar/i }));
    await screen.findByText('Sousa, João');
  }

  it('edita o nome e recarrega a lista com o valor atualizado', async () => {
    const buscarSpy = vi
      .spyOn(buscaApi, 'buscarRegistros')
      .mockResolvedValueOnce(pagina([registro()]))
      .mockResolvedValueOnce(pagina([registro({ nomeCompleto: 'Sousa, João Victor' })]));
    const alterarSpy = vi
      .spyOn(gerenciarApi, 'alterarRegistro')
      .mockResolvedValue(registro({ nomeCompleto: 'Sousa, João Victor' }));

    const user = userEvent.setup();
    render(<BuscaRegistros />);
    await buscar(user);

    await user.click(screen.getByRole('button', { name: /editar/i }));
    const form = screen.getByRole('form', { name: /editar registro/i });
    const campoNome = within(form).getByLabelText(/nome \(sobrenome/i);
    await user.clear(campoNome);
    await user.type(campoNome, 'Sousa, João Victor');
    await user.click(within(form).getByRole('button', { name: /salvar/i }));

    expect(alterarSpy).toHaveBeenCalledWith(1, {
      rm: 203,
      nomeCompleto: 'Sousa, João Victor',
      anoRegistro: null,
    });
    expect(await screen.findByText('Sousa, João Victor')).toBeInTheDocument();
    expect(buscarSpy).toHaveBeenCalledTimes(2); // busca inicial + recarga
  });

  it('desativa após confirmação e recarrega a lista', async () => {
    const buscarSpy = vi
      .spyOn(buscaApi, 'buscarRegistros')
      .mockResolvedValueOnce(pagina([registro()]))
      .mockResolvedValueOnce(pagina([]));
    const desativarSpy = vi.spyOn(gerenciarApi, 'desativarRegistro').mockResolvedValue();
    vi.spyOn(window, 'confirm').mockReturnValue(true);

    const user = userEvent.setup();
    render(<BuscaRegistros />);
    await buscar(user);

    await user.click(screen.getByRole('button', { name: /desativar/i }));

    expect(desativarSpy).toHaveBeenCalledWith(1);
    expect(await screen.findByText(/nenhum registro encontrado/i)).toBeInTheDocument();
    expect(buscarSpy).toHaveBeenCalledTimes(2);
  });

  it('não desativa se o usuário cancelar a confirmação', async () => {
    vi.spyOn(buscaApi, 'buscarRegistros').mockResolvedValue(pagina([registro()]));
    const desativarSpy = vi.spyOn(gerenciarApi, 'desativarRegistro').mockResolvedValue();
    vi.spyOn(window, 'confirm').mockReturnValue(false);

    const user = userEvent.setup();
    render(<BuscaRegistros />);
    await buscar(user);

    await user.click(screen.getByRole('button', { name: /desativar/i }));

    expect(desativarSpy).not.toHaveBeenCalled();
  });

  it('mostra erro do backend ao tentar alterar para um RM já usado', async () => {
    vi.spyOn(buscaApi, 'buscarRegistros').mockResolvedValue(pagina([registro()]));
    vi.spyOn(gerenciarApi, 'alterarRegistro').mockRejectedValue({
      message: 'RM 999 já cadastrado',
    });

    const user = userEvent.setup();
    render(<BuscaRegistros />);
    await buscar(user);

    await user.click(screen.getByRole('button', { name: /editar/i }));
    const form = screen.getByRole('form', { name: /editar registro/i });
    await user.click(within(form).getByRole('button', { name: /salvar/i }));

    expect(await screen.findByRole('alert')).toHaveTextContent('RM 999 já cadastrado');
  });
});
