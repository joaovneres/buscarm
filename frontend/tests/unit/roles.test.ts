import { describe, expect, it } from 'vitest';

import { ehAdmin, itensVisiveis, podeEscrever, rotuloPapel } from '@/shared/auth/roles';

describe('papéis e navegação', () => {
  it('classifica permissões por papel', () => {
    expect(ehAdmin('ADMIN')).toBe(true);
    expect(ehAdmin('SECRETARIA')).toBe(false);
    expect(podeEscrever('SECRETARIA')).toBe(true);
    expect(podeEscrever('VISUALIZADOR')).toBe(false);
  });

  it('mostra itens de navegação conforme o papel', () => {
    const admin = itensVisiveis('ADMIN').map((i) => i.href);
    expect(admin).toEqual(
      expect.arrayContaining(['/registros', '/importacao/planilha', '/usuarios', '/auditoria']),
    );

    const visual = itensVisiveis('VISUALIZADOR').map((i) => i.href);
    expect(visual).toEqual(['/registros']);

    const secretaria = itensVisiveis('SECRETARIA').map((i) => i.href);
    expect(secretaria).toContain('/importacao/planilha');
    expect(secretaria).not.toContain('/usuarios');
  });

  it('traduz o papel para exibição', () => {
    expect(rotuloPapel('ADMIN')).toBe('Administrador');
    expect(rotuloPapel('VISUALIZADOR')).toBe('Visualização');
  });
});
