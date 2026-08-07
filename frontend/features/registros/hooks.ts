'use client';

import { useState } from 'react';

import { buscarRegistros } from './api';
import type { RegistroResponse } from './types';

export type EstadoBusca = 'idle' | 'loading' | 'sucesso' | 'erro';

/** Encapsula a chamada à API e os estados idle/loading/sucesso/erro. */
export function useBuscaRegistros() {
  const [estado, setEstado] = useState<EstadoBusca>('idle');
  const [resultados, setResultados] = useState<RegistroResponse[]>([]);
  const [erro, setErro] = useState<string | null>(null);

  async function buscar(params: { nome?: string; rm?: string }) {
    setEstado('loading');
    setErro(null);
    try {
      const pagina = await buscarRegistros(params);
      setResultados(pagina.conteudo);
      setEstado('sucesso');
    } catch (e) {
      const mensagem = (e as { message?: string })?.message ?? 'Falha na busca';
      setErro(mensagem);
      setEstado('erro');
    }
  }

  return { estado, resultados, erro, buscar };
}
