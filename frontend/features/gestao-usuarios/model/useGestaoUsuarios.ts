'use client';

import { useCallback, useEffect, useState } from 'react';

import { criarUsuario, listarUsuarios, resetarSenha } from '@/entities/usuario/api/usuarios';
import type { NovoUsuario, UsuarioResumo } from '@/entities/usuario/model/types';

export function useGestaoUsuarios() {
  const [lista, setLista] = useState<UsuarioResumo[]>([]);
  const [estado, setEstado] = useState<'idle' | 'loading' | 'sucesso' | 'erro'>('idle');
  const [erro, setErro] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);
  const [erroAcao, setErroAcao] = useState<string | null>(null);

  const carregar = useCallback(async () => {
    setEstado('loading');
    setErro(null);
    try {
      setLista(await listarUsuarios());
      setEstado('sucesso');
    } catch (e) {
      setErro((e as { message?: string })?.message ?? 'Falha ao carregar os usuários');
      setEstado('erro');
    }
  }, []);

  useEffect(() => {
    carregar();
  }, [carregar]);

  async function acao(fn: () => Promise<unknown>): Promise<boolean> {
    setEnviando(true);
    setErroAcao(null);
    try {
      await fn();
      return true;
    } catch (e) {
      setErroAcao((e as { message?: string })?.message ?? 'Não foi possível concluir a ação');
      return false;
    } finally {
      setEnviando(false);
    }
  }

  return {
    lista,
    estado,
    erro,
    enviando,
    erroAcao,
    limparErroAcao: () => setErroAcao(null),
    async criar(dados: NovoUsuario): Promise<boolean> {
      const ok = await acao(() => criarUsuario(dados));
      if (ok) {
        await carregar();
      }
      return ok;
    },
    resetar: (id: number, senha: string) => acao(() => resetarSenha(id, senha)),
  };
}
