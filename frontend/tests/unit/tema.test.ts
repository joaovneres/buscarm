import { beforeEach, describe, expect, it } from 'vitest';

import { aplicarTema, getTema } from '@/shared/theme/tema';

describe('tema', () => {
  beforeEach(() => {
    localStorage.clear();
    document.documentElement.removeAttribute('data-theme');
  });

  it('o padrão é o tema claro', () => {
    expect(getTema()).toBe('claro');
  });

  it('aplica o escuro (data-theme), persiste e volta para o claro', () => {
    aplicarTema('escuro');
    expect(document.documentElement.getAttribute('data-theme')).toBe('dark');
    expect(getTema()).toBe('escuro');

    aplicarTema('claro');
    expect(document.documentElement.hasAttribute('data-theme')).toBe(false);
    expect(getTema()).toBe('claro');
  });
});
