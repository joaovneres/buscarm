'use client';

import { Fragment } from 'react';

import type { RegistroResponse } from '@/entities/registro/model/types';
import { podeEscrever } from '@/shared/auth/roles';
import { IconeLapis, IconeLixeira, IconeOlho } from '@/shared/ui/icones';

import { StatusBadge } from './StatusBadge';

/** Primeira letra do sobrenome, sem acento e em maiúscula (para as divisórias do índice). */
function inicialDe(registro: RegistroResponse): string {
  const base = registro.sobrenome || registro.nomeCompleto || '';
  const semAcento = base.normalize('NFD').replace(/\p{M}+/gu, '');
  const letra = semAcento.trim().charAt(0).toUpperCase();
  return /[A-Z]/.test(letra) ? letra : '#';
}

interface Props {
  registros: RegistroResponse[];
  papel?: string;
  onDetalhe: (r: RegistroResponse) => void;
  onEditar: (r: RegistroResponse) => void;
  onExcluir: (r: RegistroResponse) => void;
}

export function TabelaRegistros({ registros, papel, onDetalhe, onEditar, onExcluir }: Props) {
  if (registros.length === 0) {
    return <p className="vazio">Nenhum registro encontrado. Ajuste a busca ou os filtros.</p>;
  }

  const escreve = podeEscrever(papel);
  let letraAnterior = '';

  return (
    <div className="tabela-wrap">
      <table className="tabela-registros">
        <caption className="sr-only">Registros de matrícula</caption>
        <thead>
          <tr>
            <th scope="col">Nome</th>
            <th scope="col">RM</th>
            <th scope="col">Status</th>
            <th scope="col" className="col-acoes">
              Ações
            </th>
          </tr>
        </thead>
        <tbody>
          {registros.map((r) => {
            const letra = inicialDe(r);
            const novaLetra = letra !== letraAnterior;
            letraAnterior = letra;
            return (
              <Fragment key={r.id}>
                {novaLetra && (
                  <tr className="divisoria" aria-hidden>
                    <td colSpan={4}>{letra}</td>
                  </tr>
                )}
                <tr>
                  <td className="col-nome">{r.nomeCompleto}</td>
                  <td>
                    <span className="rm">{r.rm}</span>
                  </td>
                  <td>
                    <StatusBadge status={r.status} />
                  </td>
                  <td className="col-acoes">
                    <div className="acoes">
                      <button
                        type="button"
                        className="icone-btn"
                        title="Ver detalhes"
                        aria-label={`Ver detalhes de ${r.nomeCompleto}`}
                        onClick={() => onDetalhe(r)}
                      >
                        <IconeOlho />
                      </button>
                      {escreve && (
                        <>
                          <button
                            type="button"
                            className="icone-btn"
                            title="Editar"
                            aria-label={`Editar ${r.nomeCompleto}`}
                            onClick={() => onEditar(r)}
                          >
                            <IconeLapis />
                          </button>
                          <button
                            type="button"
                            className="icone-btn icone-btn--perigo"
                            title="Excluir"
                            aria-label={`Excluir ${r.nomeCompleto}`}
                            onClick={() => onExcluir(r)}
                          >
                            <IconeLixeira />
                          </button>
                        </>
                      )}
                    </div>
                  </td>
                </tr>
              </Fragment>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
