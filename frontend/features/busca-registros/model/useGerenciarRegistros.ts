'use client';

import { useState } from 'react';

import {
  alterarRegistro,
  desativarRegistro,
  type AlteracaoRegistro,
} from '@/entities/registro/api/alterarRegistro';

/** Ações de escrita sobre um registro (alterar/desativar), com estado de envio e erro. */
export function useGerenciarRegistros() {
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  async function alterar(id: number, dados: AlteracaoRegistro): Promise<boolean> {
    setEnviando(true);
    setErro(null);
    try {
      await alterarRegistro(id, dados);
      return true;
    } catch (e) {
      setErro((e as { message?: string })?.message ?? 'Falha ao alterar');
      return false;
    } finally {
      setEnviando(false);
    }
  }

  async function desativar(id: number): Promise<boolean> {
    setEnviando(true);
    setErro(null);
    try {
      await desativarRegistro(id);
      return true;
    } catch (e) {
      setErro((e as { message?: string })?.message ?? 'Falha ao desativar');
      return false;
    } finally {
      setEnviando(false);
    }
  }

  return { enviando, erro, alterar, desativar };
}
