import { apiGet, apiPatchSemResposta, apiPost, apiPut } from '@/shared/api/client';

import type { AlteracaoUsuario, NovoUsuario, UsuarioResumo } from '../model/types';

/** Lista todos os usuários (ADMIN). */
export function listarUsuarios(): Promise<UsuarioResumo[]> {
  return apiGet<UsuarioResumo[]>('/api/usuarios');
}

/** Cadastra um novo usuário (ADMIN). */
export function criarUsuario(dados: NovoUsuario): Promise<UsuarioResumo> {
  return apiPost<UsuarioResumo>('/api/usuarios', dados);
}

/** Altera nome e papel de um usuário (ADMIN). */
export function alterarUsuario(id: number, dados: AlteracaoUsuario): Promise<UsuarioResumo> {
  return apiPut<UsuarioResumo>(`/api/usuarios/${id}`, dados);
}

/** Admin redefine a senha de um usuário (fica provisória até ele trocá-la). */
export function resetarSenha(id: number, senha: string): Promise<void> {
  return apiPatchSemResposta(`/api/usuarios/${id}/senha`, { senha });
}
