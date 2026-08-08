import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';

import { ConferenciaOcr } from '@/features/importar-imagem/ui/ConferenciaOcr';
import type { ImportacaoResponse } from '@/entities/importacao/model/types';

const importacao: ImportacaoResponse = {
  id: 1,
  tipo: 'IMAGEM',
  nomeArquivo: 'pagina.jpg',
  status: 'VALIDADA',
  resumo: { total: 2, validos: 1, duplicados: 0, conflitos: 0, invalidos: 1, importados: 0, ignorados: 0 },
  itens: [
    { linha: 1, rm: 203, nome: 'Sousa, João Victor', situacao: 'VALIDO', mensagem: null, registroId: null },
    { linha: 2, rm: null, nome: 'Pereira, Carlos', situacao: 'INVALIDO', mensagem: 'RM ausente', registroId: null },
  ],
};

const linhas = {
  1: { rm: '203', nome: 'Sousa, João Victor', selecionado: true },
  2: { rm: '', nome: 'Pereira, Carlos', selecionado: false },
};

describe('ConferenciaOcr', () => {
  it('mostra os campos editáveis e o total selecionado', () => {
    render(
      <ConferenciaOcr
        importacao={importacao}
        linhas={linhas}
        onEditar={() => {}}
        onAlternar={() => {}}
        onConfirmar={() => {}}
        confirmando={false}
      />,
    );

    expect(screen.getByLabelText('RM da linha 1')).toHaveValue('203');
    expect(screen.getByLabelText('Nome da linha 2')).toHaveValue('Pereira, Carlos');
    expect(screen.getByLabelText('Importar linha 1')).toBeChecked();
    expect(screen.getByLabelText('Importar linha 2')).not.toBeChecked();
    expect(screen.getByRole('button', { name: /importar 1 registro/i })).toBeInTheDocument();
  });

  it('permite editar o RM reconhecido', async () => {
    const onEditar = vi.fn();
    render(
      <ConferenciaOcr
        importacao={importacao}
        linhas={linhas}
        onEditar={onEditar}
        onAlternar={() => {}}
        onConfirmar={() => {}}
        confirmando={false}
      />,
    );

    await userEvent.type(screen.getByLabelText('RM da linha 2'), '205');
    expect(onEditar).toHaveBeenCalledWith(2, 'rm', '2');
  });
});
