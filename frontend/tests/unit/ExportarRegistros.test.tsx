import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';

import { ExportarRegistros } from '@/features/exportar-registros/ui/ExportarRegistros';

describe('ExportarRegistros', () => {
  it('oferece download em .xlsx e .csv', () => {
    render(<ExportarRegistros />);

    const xlsx = screen.getByRole('link', { name: /\.xlsx/i });
    const csv = screen.getByRole('link', { name: /\.csv/i });

    expect(xlsx).toHaveAttribute('href', expect.stringContaining('formato=xlsx'));
    expect(csv).toHaveAttribute('href', expect.stringContaining('formato=csv'));
  });
});
