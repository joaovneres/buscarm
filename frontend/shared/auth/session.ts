export interface UsuarioSessao {
  nome: string;
  username: string;
  papel: string;
  senhaProvisoria?: boolean;
}

const CHAVE_TOKEN = 'buscarm.token';
const CHAVE_USUARIO = 'buscarm.usuario';

export function getToken(): string | null {
  if (typeof window === 'undefined') {
    return null;
  }
  return window.localStorage.getItem(CHAVE_TOKEN);
}

export function getUsuario(): UsuarioSessao | null {
  if (typeof window === 'undefined') {
    return null;
  }
  const bruto = window.localStorage.getItem(CHAVE_USUARIO);
  if (!bruto) {
    return null;
  }
  try {
    return JSON.parse(bruto) as UsuarioSessao;
  } catch {
    return null;
  }
}

export function salvarSessao(token: string, usuario: UsuarioSessao): void {
  window.localStorage.setItem(CHAVE_TOKEN, token);
  window.localStorage.setItem(CHAVE_USUARIO, JSON.stringify(usuario));
}

export function limparSessao(): void {
  if (typeof window === 'undefined') {
    return;
  }
  window.localStorage.removeItem(CHAVE_TOKEN);
  window.localStorage.removeItem(CHAVE_USUARIO);
}
