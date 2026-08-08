'use client';

import { useState } from 'react';

import { confirmarImportacao } from '@/entities/importacao/api/confirmarImportacao';
import { validarPlanilha } from '@/entities/importacao/api/validarPlanilha';
import type { ImportacaoResponse } from '@/entities/importacao/model/types';

export type EtapaImportacao =
  | 'idle'
  | 'validando'
  | 'previa'
  | 'confirmando'
  | 'concluida'
  | 'erro';

/** Máquina de estados do fluxo: upload → pré-visualização → confirmação → relatório. */
export function useImportarPlanilha() {
  const [etapa, setEtapa] = useState<EtapaImportacao>('idle');
  const [importacao, setImportacao] = useState<ImportacaoResponse | null>(null);
  const [selecionadas, setSelecionadas] = useState<number[]>([]);
  const [erro, setErro] = useState<string | null>(null);

  async function validar(arquivo: File) {
    setEtapa('validando');
    setErro(null);
    try {
      const resultado = await validarPlanilha(arquivo);
      setImportacao(resultado);
      setSelecionadas(
        resultado.itens.filter((i) => i.situacao === 'VALIDO').map((i) => i.linha),
      );
      setEtapa('previa');
    } catch (e) {
      setErro((e as { message?: string })?.message ?? 'Falha ao validar a planilha');
      setEtapa('erro');
    }
  }

  function alternarLinha(linha: number) {
    setSelecionadas((atual) =>
      atual.includes(linha) ? atual.filter((l) => l !== linha) : [...atual, linha],
    );
  }

  async function confirmar() {
    if (!importacao) {
      return;
    }
    setEtapa('confirmando');
    setErro(null);
    try {
      const resultado = await confirmarImportacao(importacao.id, selecionadas);
      setImportacao(resultado);
      setEtapa('concluida');
    } catch (e) {
      setErro((e as { message?: string })?.message ?? 'Falha ao confirmar a importação');
      setEtapa('erro');
    }
  }

  function reiniciar() {
    setEtapa('idle');
    setImportacao(null);
    setSelecionadas([]);
    setErro(null);
  }

  return { etapa, importacao, selecionadas, erro, validar, alternarLinha, confirmar, reiniciar };
}
