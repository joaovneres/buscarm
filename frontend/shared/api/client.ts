import { getToken, limparSessao } from '@/shared/auth/session';

const BASE_URL = process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:8080';

export interface ApiError {
  status: number;
  code: string;
  message: string;
}

function cabecalhos(extra?: Record<string, string>): Record<string, string> {
  const headers: Record<string, string> = { Accept: 'application/json', ...extra };
  const token = getToken();
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }
  return headers;
}

async function tratar<T>(res: Response): Promise<T> {
  if (!res.ok) {
    if (res.status === 401) {
      limparSessao();
    }
    let corpo: { code?: string; message?: string } | null = null;
    try {
      corpo = (await res.json()) as { code?: string; message?: string };
    } catch {
      corpo = null;
    }
    const erro: ApiError = {
      status: res.status,
      code: corpo?.code ?? 'ERRO',
      message: corpo?.message ?? 'Erro ao consultar a API',
    };
    throw erro;
  }
  return (await res.json()) as T;
}

/** URL absoluta da API. */
export function urlApi(path: string, params?: Record<string, string>): string {
  const url = new URL(path, BASE_URL);
  if (params) {
    Object.entries(params).forEach(([chave, valor]) => {
      if (valor) {
        url.searchParams.set(chave, valor);
      }
    });
  }
  return url.toString();
}

export async function apiGet<T>(path: string, params?: Record<string, string>): Promise<T> {
  const res = await fetch(urlApi(path, params), { headers: cabecalhos() });
  return tratar<T>(res);
}

export async function apiPost<T>(path: string, body: unknown): Promise<T> {
  const res = await fetch(urlApi(path), {
    method: 'POST',
    headers: cabecalhos({ 'Content-Type': 'application/json' }),
    body: JSON.stringify(body),
  });
  return tratar<T>(res);
}

export async function apiPut<T>(path: string, body: unknown): Promise<T> {
  const res = await fetch(urlApi(path), {
    method: 'PUT',
    headers: cabecalhos({ 'Content-Type': 'application/json' }),
    body: JSON.stringify(body),
  });
  return tratar<T>(res);
}

/** DELETE que não espera corpo de resposta (204 No Content). */
export async function apiDelete(path: string): Promise<void> {
  const res = await fetch(urlApi(path), { method: 'DELETE', headers: cabecalhos() });
  if (!res.ok) {
    if (res.status === 401) {
      limparSessao();
    }
    let corpo: { code?: string; message?: string } | null = null;
    try {
      corpo = (await res.json()) as { code?: string; message?: string };
    } catch {
      corpo = null;
    }
    throw {
      status: res.status,
      code: corpo?.code ?? 'ERRO',
      message: corpo?.message ?? 'Erro ao consultar a API',
    } as ApiError;
  }
}

export async function apiPostForm<T>(path: string, form: FormData): Promise<T> {
  const res = await fetch(urlApi(path), {
    method: 'POST',
    headers: cabecalhos(),
    body: form,
  });
  return tratar<T>(res);
}

/** Baixa um arquivo autenticado (envia o token no header e dispara o download). */
export async function baixarArquivo(
  path: string,
  params: Record<string, string>,
  nomePadrao: string,
): Promise<void> {
  const res = await fetch(urlApi(path, params), { headers: cabecalhos() });
  if (!res.ok) {
    if (res.status === 401) {
      limparSessao();
    }
    throw { status: res.status, code: 'ERRO', message: 'Falha no download' } as ApiError;
  }
  const blob = await res.blob();
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = nomePadrao;
  document.body.appendChild(a);
  a.click();
  a.remove();
  URL.revokeObjectURL(url);
}
