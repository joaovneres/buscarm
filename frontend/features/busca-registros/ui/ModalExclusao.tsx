'use client';

import type { RegistroResponse } from '@/entities/registro/model/types';
import { Modal } from '@/shared/ui/Modal';

interface Props {
  aberto: boolean;
  registro: RegistroResponse | null;
  enviando: boolean;
  erro: string | null;
  onConfirmar: (r: RegistroResponse) => void;
  onFechar: () => void;
}

export function ModalExclusao({ aberto, registro, enviando, erro, onConfirmar, onFechar }: Props) {
  if (!registro) {
    return null;
  }
  return (
    <Modal titulo="Excluir registro" aberto={aberto} onFechar={onFechar}>
      <p>
        Excluir o registro de <strong>{registro.nomeCompleto}</strong> (RM{' '}
        <span className="rm">{registro.rm}</span>)? Ele deixa de aparecer nas buscas — o prontuário
        físico continua existindo.
      </p>

      {erro && <p role="alert">{erro}</p>}

      <div className="modal__acoes">
        <button
          type="button"
          className="btn-perigo"
          onClick={() => onConfirmar(registro)}
          disabled={enviando}
        >
          {enviando ? 'Excluindo…' : 'Excluir'}
        </button>
        <button type="button" className="btn-secundario" onClick={onFechar} disabled={enviando}>
          Cancelar
        </button>
      </div>
    </Modal>
  );
}
