import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';

import { FormularioRegistro } from '@/features/busca-registros/ui/FormularioRegistro';
import type { RegistroResponse } from '@/entities/registro/model/types';

const base = { enviando: false, erro: null, onCancelar: () => {} };

function registro(over: Partial<RegistroResponse> = {}): RegistroResponse {
  return {
    id: 1,
    rm: 203,
    nomeCompleto: 'Sousa, Ana',
    sobrenome: 'Sousa',
    anoRegistro: 2020,
    ativo: true,
    status: 'VALIDADO',
    criadoPor: 'sec',
    atualizadoPor: 'sec',
    criadoEm: '2026-01-01T00:00:00Z',
    atualizadoEm: null,
    ...over,
  };
}

describe('FormularioRegistro', () => {
  it('cadastra enviando anoRegistro null quando vazio', async () => {
    const onSalvar = vi.fn();
    const user = userEvent.setup();
    render(<FormularioRegistro {...base} onSalvar={onSalvar} />);

    await user.type(screen.getByLabelText('RM'), '900');
    await user.type(screen.getByLabelText('Nome completo'), 'Rocha, Ian');
    await user.click(screen.getByRole('button', { name: /cadastrar/i }));

    expect(onSalvar).toHaveBeenCalledWith({ rm: 900, nomeCompleto: 'Rocha, Ian', anoRegistro: null });
  });

  it('edita com os dados preenchidos', async () => {
    const onSalvar = vi.fn();
    const user = userEvent.setup();
    render(<FormularioRegistro {...base} registro={registro()} onSalvar={onSalvar} />);

    expect(screen.getByLabelText('RM')).toHaveValue('203');
    await user.click(screen.getByRole('button', { name: /salvar alterações/i }));

    expect(onSalvar).toHaveBeenCalledWith({ rm: 203, nomeCompleto: 'Sousa, Ana', anoRegistro: 2020 });
  });
});
