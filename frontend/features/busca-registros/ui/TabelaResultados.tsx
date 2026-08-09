'use client';

import type { AlteracaoRegistro } from '@/entities/registro/api/alterarRegistro';
import type { RegistroResponse } from '@/entities/registro/model/types';

import { EditarRegistroForm } from './EditarRegistroForm';

interface Props {
  registros: RegistroResponse[];
  editandoId: number | null;
  enviando: boolean;
  erroAcao: string | null;
  onIniciarEdicao: (id: number) => void;
  onCancelarEdicao: () => void;
  onSalvar: (id: number, dados: AlteracaoRegistro) => void;
  onDesativar: (registro: RegistroResponse) => void;
}

export function TabelaResultados({
  registros,
  editandoId,
  enviando,
  erroAcao,
  onIniciarEdicao,
  onCancelarEdicao,
  onSalvar,
  onDesativar,
}: Props) {
  if (registros.length === 0) {
    return <p>Nenhum registro encontrado.</p>;
  }

  return (
    <table>
      <caption>Resultados da busca</caption>
      <thead>
        <tr>
          <th scope="col">RM</th>
          <th scope="col">Nome</th>
          <th scope="col">Ano</th>
          <th scope="col">Ações</th>
        </tr>
      </thead>
      <tbody>
        {registros.map((r) =>
          editandoId === r.id ? (
            <tr key={r.id}>
              <td colSpan={4}>
                <EditarRegistroForm
                  registro={r}
                  enviando={enviando}
                  erro={erroAcao}
                  onSalvar={(dados) => onSalvar(r.id, dados)}
                  onCancelar={onCancelarEdicao}
                />
              </td>
            </tr>
          ) : (
            <tr key={r.id}>
              <td>
                <strong>{r.rm}</strong>
              </td>
              <td>{r.nomeCompleto}</td>
              <td>{r.anoRegistro ?? '—'}</td>
              <td>
                <button type="button" onClick={() => onIniciarEdicao(r.id)}>
                  Editar
                </button>
                <button type="button" className="btn-danger" onClick={() => onDesativar(r)}>
                  Desativar
                </button>
              </td>
            </tr>
          ),
        )}
      </tbody>
    </table>
  );
}
