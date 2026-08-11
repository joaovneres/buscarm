'use client';

import { useEffect, useRef, type ReactNode } from 'react';

interface Props {
  titulo: string;
  aberto: boolean;
  onFechar: () => void;
  children: ReactNode;
}

/** Diálogo acessível baseado no elemento <dialog> nativo (Esc fecha, foco preso, backdrop). */
export function Modal({ titulo, aberto, onFechar, children }: Props) {
  const ref = useRef<HTMLDialogElement>(null);

  useEffect(() => {
    const dialog = ref.current;
    if (!dialog || !aberto) {
      return;
    }
    if (!dialog.open) {
      try {
        dialog.showModal();
      } catch {
        dialog.setAttribute('open', '');
      }
    }
  }, [aberto]);

  if (!aberto) {
    return null;
  }

  return (
    <dialog
      ref={ref}
      className="modal"
      aria-label={titulo}
      onCancel={(e) => {
        e.preventDefault();
        onFechar();
      }}
      onClick={(e) => {
        if (e.target === ref.current) {
          onFechar();
        }
      }}
    >
      <div className="modal__cabecalho">
        <h2>{titulo}</h2>
        <button type="button" className="modal__fechar" aria-label="Fechar" onClick={onFechar}>
          ×
        </button>
      </div>
      <div className="modal__corpo">{children}</div>
    </dialog>
  );
}
