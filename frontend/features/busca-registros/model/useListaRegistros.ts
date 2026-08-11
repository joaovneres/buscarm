'use client';

import { useCallback, useEffect, useState } from 'react';

import { buscarRegistros } from '@/entities/registro/api/buscarRegistros';
import type {
  FiltrosBusca,
  PaginaResponse,
  RegistroResponse,
} from '@/entities/registro/model/types';

const TAMANHO = 20;

export type EstadoLista = 'idle' | 'loading' | 'sucesso' | 'erro';

/** Estado da listagem: filtros + paginação + dados, recarregável após ações. */
export function useListaRegistros() {
  const [filtros, setFiltros] = useState<FiltrosBusca>({});
  const [pagina, setPagina] = useState(0);
  const [dados, setDados] = useState<PaginaResponse<RegistroResponse> | null>(null);
  const [estado, setEstado] = useState<EstadoLista>('idle');
  const [erro, setErro] = useState<string | null>(null);

  const carregar = useCallback(async (f: FiltrosBusca, p: number) => {
    setEstado('loading');
    setErro(null);
    try {
      setDados(await buscarRegistros({ ...f, pagina: p, tamanho: TAMANHO }));
      setEstado('sucesso');
    } catch (e) {
      setErro((e as { message?: string })?.message ?? 'Falha ao carregar os registros');
      setEstado('erro');
    }
  }, []);

  useEffect(() => {
    carregar(filtros, pagina);
  }, [filtros, pagina, carregar]);

  function aplicarFiltros(novos: FiltrosBusca) {
    setPagina(0);
    setFiltros(novos);
  }

  function irPara(p: number) {
    setPagina(p);
  }

  function recarregar() {
    carregar(filtros, pagina);
  }

  return { filtros, pagina, dados, estado, erro, aplicarFiltros, irPara, recarregar };
}
