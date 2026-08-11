import { apiPost } from '@/shared/api/client';

export interface LoginResponse {
  token: string;
  nome: string;
  username: string;
  papel: string;
  senhaProvisoria: boolean;
}

export function login(username: string, senha: string): Promise<LoginResponse> {
  return apiPost<LoginResponse>('/api/auth/login', { username, senha });
}
