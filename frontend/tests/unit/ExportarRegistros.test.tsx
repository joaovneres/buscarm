import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { ExportarRegistros } from '@/features/exportar-registros/ui/ExportarRegistros';
import { baixarArquivo } from '@/shared/api/client';

vi.mock('@/shared/api/client', () => ({ baixarArquivo: vi.fn() }));

describe('ExportarRegistros', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('baixa a exportação em .xlsx e .csv', async () => {
    const user = userEvent.setup();
    render(<ExportarRegistros />);

    await user.click(screen.getByRole('button', { name: /\.xlsx/i }));
    expect(baixarArquivo).toHaveBeenCalledWith(
      '/api/registros/exportar',
      { formato: 'xlsx' },
      expect.stringContaining('xlsx'),
    );

    await user.click(screen.getByRole('button', { name: /\.csv/i }));
    expect(baixarArquivo).toHaveBeenCalledWith(
      '/api/registros/exportar',
      { formato: 'csv' },
      expect.stringContaining('csv'),
    );
  });
});
