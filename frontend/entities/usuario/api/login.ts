import { apiPost } from '@/shared/api/client';

export interface LoginResponse {
  token: string;
  nome: string;
  email: string;
  papel: string;
}

export function login(email: string, senha: string): Promise<LoginResponse> {
  return apiPost<LoginResponse>('/api/auth/login', { email, senha });
}
