'use client';

import { useState } from 'react';

import { confirmarImagem } from '@/entities/importacao/api/confirmarImagem';
import { processarImagem } from '@/entities/importacao/api/processarImagem';
import type { ImportacaoResponse } from '@/entities/importacao/model/types';

export type EtapaImagem =
  | 'idle'
  | 'processando'
  | 'conferencia'
  | 'confirmando'
  | 'concluida'
  | 'erro';

export interface LinhaEditavel {
  rm: string;
  nome: string;
  selecionado: boolean;
}

export type MapaLinhas = Record<number, LinhaEditavel>;

/** Fluxo do OCR: foto → conferência (edição do texto reconhecido) → confirmação → relatório. */
export function useImportarImagem() {
  const [etapa, setEtapa] = useState<EtapaImagem>('idle');
  const [importacao, setImportacao] = useState<ImportacaoResponse | null>(null);
  const [linhas, setLinhas] = useState<MapaLinhas>({});
  const [erro, setErro] = useState<string | null>(null);

  async function processar(arquivo: File) {
    setEtapa('processando');
    setErro(null);
    try {
      const resultado = await processarImagem(arquivo);
      const mapa: MapaLinhas = {};
      resultado.itens.forEach((item) => {
        mapa[item.linha] = {
          rm: item.rm !== null ? String(item.rm) : '',
          nome: item.nome ?? '',
          selecionado: item.situacao === 'VALIDO',
        };
      });
      setImportacao(resultado);
      setLinhas(mapa);
      setEtapa('conferencia');
    } catch (e) {
      setErro((e as { message?: string })?.message ?? 'Falha ao processar a foto');
      setEtapa('erro');
    }
  }

  function editar(linha: number, campo: 'rm' | 'nome', valor: string) {
    setLinhas((atual) => ({ ...atual, [linha]: { ...atual[linha], [campo]: valor } }));
  }

  function alternar(linha: number) {
    setLinhas((atual) => ({
      ...atual,
      [linha]: { ...atual[linha], selecionado: !atual[linha].selecionado },
    }));
  }

  async function confirmar() {
    if (!importacao) {
      return;
    }
    const itens = Object.entries(linhas)
      .filter(([, l]) => l.selecionado)
      .map(([linha, l]) => ({
        linha: Number(linha),
        rm: l.rm.trim() === '' ? null : Number(l.rm),
        nomeCompleto: l.nome.trim(),
      }));

    setEtapa('confirmando');
    setErro(null);
    try {
      const resultado = await confirmarImagem(importacao.id, itens);
      setImportacao(resultado);
      setEtapa('concluida');
    } catch (e) {
      setErro((e as { message?: string })?.message ?? 'Falha ao confirmar');
      setEtapa('erro');
    }
  }

  function reiniciar() {
    setEtapa('idle');
    setImportacao(null);
    setLinhas({});
    setErro(null);
  }

  return { etapa, importacao, linhas, erro, processar, editar, alternar, confirmar, reiniciar };
}
