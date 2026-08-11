import type { Papel } from '@/shared/auth/roles';

export interface UsuarioResumo {
  id: number;
  nome: string;
  username: string;
  papel: string;
  ativo: boolean;
}

export interface NovoUsuario {
  nome: string;
  username: string;
  senha: string;
  papel: Papel;
}

export interface AlteracaoUsuario {
  nome: string;
  papel: Papel;
}
