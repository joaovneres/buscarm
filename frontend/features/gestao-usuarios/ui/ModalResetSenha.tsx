'use client';

import { useState, type FormEvent } from 'react';

import type { UsuarioResumo } from '@/entities/usuario/model/types';
import { Modal } from '@/shared/ui/Modal';

interface Props {
  aberto: boolean;
  usuario: UsuarioResumo | null;
  enviando: boolean;
  erro: string | null;
  onConfirmar: (id: number, senha: string) => void;
  onFechar: () => void;
}

export function ModalResetSenha({ aberto, usuario, enviando, erro, onConfirmar, onFechar }: Props) {
  const [senha, setSenha] = useState('');

  if (!usuario) {
    return null;
  }

  function onSubmit(e: FormEvent) {
    e.preventDefault();
    if (senha.length >= 8 && usuario) {
      onConfirmar(usuario.id, senha);
    }
  }

  return (
    <Modal titulo="Redefinir senha" aberto={aberto} onFechar={onFechar}>
      <form onSubmit={onSubmit} aria-label="Redefinir senha">
        <p>
          Defina uma senha provisória para <strong>{usuario.nome}</strong> (
          <span className="rm">{usuario.username}</span>). Ele precisará trocá-la no próximo acesso.
        </p>

        <label htmlFor="reset-senha">Nova senha provisória (mín. 8 caracteres)</label>
        <input
          id="reset-senha"
          type="text"
          value={senha}
          onChange={(e) => setSenha(e.target.value)}
          autoComplete="off"
          required
        />

        {erro && <p role="alert">{erro}</p>}

        <div className="modal__acoes">
          <button type="submit" disabled={enviando || senha.length < 8}>
            {enviando ? 'Redefinindo…' : 'Redefinir senha'}
          </button>
          <button type="button" className="btn-secundario" onClick={onFechar} disabled={enviando}>
            Cancelar
          </button>
        </div>
      </form>
    </Modal>
  );
}
