'use client';

import { useEffect, useState } from 'react';

import { listarAuditoria } from '@/entities/auditoria/api/listarAuditoria';
import type { RegistroAuditoria } from '@/entities/auditoria/model/types';

type Estado = 'loading' | 'sucesso' | 'erro';

export function useAuditoria() {
  const [estado, setEstado] = useState<Estado>('loading');
  const [itens, setItens] = useState<RegistroAuditoria[]>([]);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    let ativo = true;
    listarAuditoria()
      .then((pagina) => {
        if (ativo) {
          setItens(pagina.conteudo);
          setEstado('sucesso');
        }
      })
      .catch((e: { message?: string }) => {
        if (ativo) {
          setErro(e?.message ?? 'Falha ao carregar a auditoria');
          setEstado('erro');
        }
      });
    return () => {
      ativo = false;
    };
  }, []);

  return { estado, itens, erro };
}
