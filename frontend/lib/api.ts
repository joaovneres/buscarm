const BASE_URL = process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:8080';

export interface ApiError {
  status: number;
  code: string;
  message: string;
}

/** Cliente central de leitura da API. Trata erro no formato ApiError do backend. */
export async function apiGet<T>(path: string, params?: Record<string, string>): Promise<T> {
  const url = new URL(path, BASE_URL);
  if (params) {
    Object.entries(params).forEach(([chave, valor]) => {
      if (valor) {
        url.searchParams.set(chave, valor);
      }
    });
  }

  const res = await fetch(url.toString(), {
    headers: { Accept: 'application/json' },
  });

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
