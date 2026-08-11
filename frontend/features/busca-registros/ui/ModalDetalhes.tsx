'use client';

import type { RegistroResponse } from '@/entities/registro/model/types';
import { podeEscrever } from '@/shared/auth/roles';
import { IconeCheck } from '@/shared/ui/icones';
import { Modal } from '@/shared/ui/Modal';

import { StatusBadge } from './StatusBadge';

function dataHora(iso: string | null): string {
  if (!iso) return '—';
  return new Date(iso).toLocaleString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

interface Props {
  aberto: boolean;
  registro: RegistroResponse | null;
  papel?: string;
  enviando: boolean;
  onValidar: (r: RegistroResponse) => void;
  onFechar: () => void;
}

export function ModalDetalhes({ aberto, registro, papel, enviando, onValidar, onFechar }: Props) {
  if (!registro) {
    return null;
  }
  const podeValidar = podeEscrever(papel) && registro.status === 'PENDENTE';

  return (
    <Modal titulo={registro.nomeCompleto} aberto={aberto} onFechar={onFechar}>
      <dl className="detalhes">
        <div>
          <dt>RM</dt>
          <dd>
            <span className="rm">{registro.rm}</span>
          </dd>
        </div>
        <div>
          <dt>Ano do registro</dt>
          <dd>{registro.anoRegistro ?? '—'}</dd>
        </div>
        <div>
          <dt>Status</dt>
          <dd>
            <StatusBadge status={registro.status} />
          </dd>
        </div>
        <div>
          <dt>Cadastrado por</dt>
          <dd>
            {registro.criadoPor} · {dataHora(registro.criadoEm)}
          </dd>
        </div>
        <div>
          <dt>Última edição</dt>
          <dd>
            {registro.atualizadoPor} · {dataHora(registro.atualizadoEm)}
          </dd>
        </div>
      </dl>

      <div className="modal__acoes">
        {podeValidar && (
          <button type="button" onClick={() => onValidar(registro)} disabled={enviando}>
            <IconeCheck /> Marcar como validado
          </button>
        )}
        <button type="button" className="btn-secundario" onClick={onFechar}>
          Fechar
        </button>
      </div>
    </Modal>
  );
}
