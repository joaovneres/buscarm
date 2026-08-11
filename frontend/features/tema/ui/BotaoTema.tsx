'use client';

import { useEffect, useState } from 'react';

import { aplicarTema, getTema, type Tema } from '@/shared/theme/tema';
import { IconeLua, IconeSol } from '@/shared/ui/icones';

/** Alterna entre tema claro e escuro (persiste a escolha). */
export function BotaoTema() {
  const [tema, setTema] = useState<Tema>('claro');

  useEffect(() => {
    setTema(getTema());
  }, []);

  function alternar() {
    const novo: Tema = tema === 'escuro' ? 'claro' : 'escuro';
    setTema(novo);
    aplicarTema(novo);
  }

  const escuro = tema === 'escuro';
  return (
    <button
      type="button"
      className="icone-btn"
      onClick={alternar}
      aria-pressed={escuro}
      aria-label={escuro ? 'Ativar tema claro' : 'Ativar tema escuro'}
      title={escuro ? 'Tema claro' : 'Tema escuro'}
    >
      {escuro ? <IconeSol /> : <IconeLua />}
    </button>
  );
}
