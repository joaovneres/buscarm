'use client';

import type { RegistroResponse } from '@/entities/registro/model/types';
import { Modal } from '@/shared/ui/Modal';

import { FormularioRegistro, type DadosRegistro } from './FormularioRegistro';

interface Props {
  aberto: boolean;
  /** Presente = edição (form preenchido); ausente = cadastro novo. */
  registro?: RegistroResponse;
  enviando: boolean;
  erro: string | null;
  onSalvar: (dados: DadosRegistro) => void;
  onFechar: () => void;
}

/** Modal único de cadastro/edição — muda só o título e os dados iniciais. */
export function ModalRegistro({ aberto, registro, enviando, erro, onSalvar, onFechar }: Props) {
  return (
    <Modal titulo={registro ? 'Editar registro' : 'Novo registro'} aberto={aberto} onFechar={onFechar}>
      <FormularioRegistro
        registro={registro}
        enviando={enviando}
        erro={erro}
        onSalvar={onSalvar}
        onCancelar={onFechar}
      />
    </Modal>
  );
}
