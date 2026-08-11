'use client';

import { useState } from 'react';

import {
  alterarRegistro,
  desativarRegistro,
  validarRegistro,
} from '@/entities/registro/api/alterarRegistro';
import { cadastrarRegistro } from '@/entities/registro/api/cadastrarRegistro';

import type { DadosRegistro } from '../ui/FormularioRegistro';

/** Ações de escrita sobre um registro, com estado de envio e erro compartilhados. */
export function useAcoesRegistro() {
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  async function executar(fn: () => Promise<unknown>): Promise<boolean> {
    setEnviando(true);
    setErro(null);
    try {
      await fn();
      return true;
    } catch (e) {
      setErro((e as { message?: string })?.message ?? 'Não foi possível concluir a ação');
      return false;
    } finally {
      setEnviando(false);
    }
  }

  return {
    enviando,
    erro,
    limparErro: () => setErro(null),
    cadastrar: (dados: DadosRegistro) => executar(() => cadastrarRegistro(dados)),
    alterar: (id: number, dados: DadosRegistro) => executar(() => alterarRegistro(id, dados)),
    desativar: (id: number) => executar(() => desativarRegistro(id)),
    validar: (id: number) => executar(() => validarRegistro(id)),
  };
}
