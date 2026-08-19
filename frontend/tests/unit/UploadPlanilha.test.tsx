import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';

import { UploadPlanilha } from '@/features/importar-planilha/ui/UploadPlanilha';

describe('UploadPlanilha', () => {
  it('anuncia suporte a .xlsx e .csv e aceita ambos no seletor', () => {
    render(<UploadPlanilha onValidar={() => {}} validando={false} />);

    const input = screen.getByLabelText(/Planilha de registros/i);
    const accept = input.getAttribute('accept') ?? '';
    expect(accept).toContain('.xlsx');
    expect(accept).toContain('.csv');
  });

  it('valida o arquivo CSV selecionado ao enviar', async () => {
    const onValidar = vi.fn();
    render(<UploadPlanilha onValidar={onValidar} validando={false} />);

    const arquivo = new File(['RM,Nome\n203,"Sousa, João"'], 'alunos.csv', { type: 'text/csv' });
    await userEvent.upload(screen.getByLabelText(/Planilha de registros/i), arquivo);
    await userEvent.click(screen.getByRole('button', { name: /validar planilha/i }));

    expect(onValidar).toHaveBeenCalledOnce();
    expect(onValidar).toHaveBeenCalledWith(arquivo);
  });

  it('mantém o botão desabilitado enquanto valida', () => {
    render(<UploadPlanilha onValidar={() => {}} validando={true} />);

    expect(screen.getByRole('button', { name: /validando/i })).toBeDisabled();
    expect(screen.getByRole('status')).toBeInTheDocument();
  });
});
