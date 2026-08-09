'use client';

import { useState } from 'react';

import { cadastrarRegistro, type NovoRegistro } from '@/entities/registro/api/cadastrarRegistro';
import type { RegistroResponse } from '@/entities/registro/model/types';

/** Encapsula o POST de cadastro com estados de envio, sucesso e erro. */
export function useCadastrarRegistro() {
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);
  const [criado, setCriado] = useState<RegistroResponse | null>(null);

  async function cadastrar(dados: NovoRegistro): Promise<boolean> {
    setEnviando(true);
    setErro(null);
    setCriado(null);
    try {
      const registro = await cadastrarRegistro(dados);
      setCriado(registro);
      return true;
    } catch (e) {
      setErro((e as { message?: string })?.message ?? 'Falha ao cadastrar');
      return false;
    } finally {
      setEnviando(false);
    }
  }

  function limpar() {
    setErro(null);
    setCriado(null);
  }

  return { enviando, erro, criado, cadastrar, limpar };
}
