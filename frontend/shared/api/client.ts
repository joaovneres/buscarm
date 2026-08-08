const BASE_URL = process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:8080';

export interface ApiError {
  status: number;
  code: string;
  message: string;
}

async function tratar<T>(res: Response): Promise<T> {
  if (!res.ok) {
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

/** URL absoluta da API (para downloads via link/âncora). */
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

/** Leitura (GET) com query params. */
export async function apiGet<T>(path: string, params?: Record<string, string>): Promise<T> {
  const url = new URL(path, BASE_URL);
  if (params) {
    Object.entries(params).forEach(([chave, valor]) => {
      if (valor) {
        url.searchParams.set(chave, valor);
      }
    });
  }
  const res = await fetch(url.toString(), { headers: { Accept: 'application/json' } });
  return tratar<T>(res);
}

/** POST com corpo JSON. */
export async function apiPost<T>(path: string, body: unknown): Promise<T> {
  const res = await fetch(new URL(path, BASE_URL).toString(), {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
    body: JSON.stringify(body),
  });
  return tratar<T>(res);
}

/** POST multipart (upload de arquivo). */
export async function apiPostForm<T>(path: string, form: FormData): Promise<T> {
  const res = await fetch(new URL(path, BASE_URL).toString(), {
    method: 'POST',
    headers: { Accept: 'application/json' },
    body: form,
  });
  return tratar<T>(res);
}
