import { apiPatchSemResposta } from '@/shared/api/client';

/** O próprio usuário troca a sua senha (informando a atual). */
export function trocarSenha(senhaAtual: string, novaSenha: string): Promise<void> {
  return apiPatchSemResposta('/api/conta/senha', { senhaAtual, novaSenha });
}
