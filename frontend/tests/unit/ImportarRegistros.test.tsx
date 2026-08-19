import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';

import { ImportarRegistros } from '@/features/importar-registros/ui/ImportarRegistros';

describe('ImportarRegistros', () => {
  it('oferece atalhos para importar planilha e por foto', () => {
    render(<ImportarRegistros />);

    const planilha = screen.getByRole('link', { name: /importar planilha/i });
    const foto = screen.getByRole('link', { name: /importar por foto/i });

    expect(planilha).toHaveAttribute('href', '/importacao/planilha');
    expect(foto).toHaveAttribute('href', '/importacao/imagem');
  });
});
