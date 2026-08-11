export type Tema = 'claro' | 'escuro';

const CHAVE = 'buscarm.tema';

/** Lê o tema salvo (padrão: claro). */
export function getTema(): Tema {
  if (typeof window === 'undefined') {
    return 'claro';
  }
  return window.localStorage.getItem(CHAVE) === 'escuro' ? 'escuro' : 'claro';
}

/** Aplica o tema no <html> (data-theme) e persiste. */
export function aplicarTema(tema: Tema): void {
  if (typeof document === 'undefined') {
    return;
  }
  if (tema === 'escuro') {
    document.documentElement.setAttribute('data-theme', 'dark');
  } else {
    document.documentElement.removeAttribute('data-theme');
  }
  window.localStorage.setItem(CHAVE, tema);
}
