import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';

import { PreviewImportacao } from '@/features/importar-planilha/ui/PreviewImportacao';
import type { ImportacaoResponse } from '@/entities/importacao/model/types';

const importacao: ImportacaoResponse = {
  id: 1,
  tipo: 'PLANILHA',
  nomeArquivo: 'alunos.xlsx',
  status: 'VALIDADA',
  resumo: { total: 3, validos: 2, duplicados: 0, conflitos: 0, invalidos: 1, importados: 0, ignorados: 0 },
  itens: [
    { linha: 2, rm: 203, nome: 'Sousa, João Victor', situacao: 'VALIDO', mensagem: null, registroId: null },
    { linha: 3, rm: 390, nome: 'Souza, Davi', situacao: 'VALIDO', mensagem: null, registroId: null },
    { linha: 4, rm: null, nome: 'Sem RM', situacao: 'INVALIDO', mensagem: 'RM ausente', registroId: null },
  ],
};

describe('PreviewImportacao', () => {
  it('lista os itens e desabilita a seleção dos inválidos', () => {
    render(
      <PreviewImportacao
        importacao={importacao}
        selecionadas={[2, 3]}
        onAlternarLinha={() => {}}
        onConfirmar={() => {}}
        confirmando={false}
      />,
    );

    expect(screen.getByText('Sousa, João Victor')).toBeInTheDocument();
    expect(screen.getByLabelText('Importar linha 2')).toBeChecked();
    expect(screen.getByLabelText('Importar linha 4')).toBeDisabled();
    expect(screen.getByText(/RM ausente/)).toBeInTheDocument();
  });

  it('confirma com os itens selecionados', async () => {
    const onConfirmar = vi.fn();
    render(
      <PreviewImportacao
        importacao={importacao}
        selecionadas={[2, 3]}
        onAlternarLinha={() => {}}
        onConfirmar={onConfirmar}
        confirmando={false}
      />,
    );

    await userEvent.click(screen.getByRole('button', { name: /importar 2 registro/i }));
    expect(onConfirmar).toHaveBeenCalledOnce();
  });
});
