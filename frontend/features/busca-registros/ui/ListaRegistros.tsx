'use client';

import { useState } from 'react';

import type { RegistroResponse } from '@/entities/registro/model/types';
import { useAuth } from '@/features/auth/model/AuthContext';
import { ExportarRegistros } from '@/features/exportar-registros/ui/ExportarRegistros';
import { ImportarRegistros } from '@/features/importar-registros/ui/ImportarRegistros';
import { podeEscrever } from '@/shared/auth/roles';

import { useAcoesRegistro } from '../model/useAcoesRegistro';
import { useListaRegistros } from '../model/useListaRegistros';
import { BarraBusca } from './BarraBusca';
import type { DadosRegistro } from './FormularioRegistro';
import { ModalDetalhes } from './ModalDetalhes';
import { ModalExclusao } from './ModalExclusao';
import { ModalRegistro } from './ModalRegistro';
import { Paginacao } from './Paginacao';
import { TabelaRegistros } from './TabelaRegistros';

type ModalAberto = 'criar' | 'editar' | 'detalhe' | 'excluir' | null;

export function ListaRegistros() {
  const { usuario } = useAuth();
  const papel = usuario?.papel;
  const escreve = podeEscrever(papel);

  const { pagina, dados, estado, erro, aplicarFiltros, irPara, recarregar } = useListaRegistros();
  const acoes = useAcoesRegistro();

  const [modal, setModal] = useState<ModalAberto>(null);
  const [alvo, setAlvo] = useState<RegistroResponse | null>(null);

  function abrir(tipo: ModalAberto, registro: RegistroResponse | null) {
    acoes.limparErro();
    setAlvo(registro);
    setModal(tipo);
  }

  function fechar() {
    setModal(null);
  }

  async function salvar(d: DadosRegistro) {
    const ok = modal === 'editar' && alvo ? await acoes.alterar(alvo.id, d) : await acoes.cadastrar(d);
    if (ok) {
      fechar();
      recarregar();
    }
  }

  async function confirmarExclusao(r: RegistroResponse) {
    if (await acoes.desativar(r.id)) {
      fechar();
      recarregar();
    }
  }

  async function validar(r: RegistroResponse) {
    if (await acoes.validar(r.id)) {
      fechar();
      recarregar();
    }
  }

  return (
    <section className="lista" aria-label="Registros de matrícula">
      <div className="lista__topo">
        {escreve && (
          <button type="button" onClick={() => abrir('criar', null)}>
            + Novo registro
          </button>
        )}
        <div className="lista__topo-acoes">
          {escreve && <ImportarRegistros />}
          <ExportarRegistros />
        </div>
      </div>

      <BarraBusca onAplicar={aplicarFiltros} />

      {estado === 'loading' && <p role="status">Carregando…</p>}
      {estado === 'erro' && <p role="alert">{erro}</p>}

      {estado === 'sucesso' && dados && (
        <>
          <TabelaRegistros
            registros={dados.conteudo}
            papel={papel}
            onDetalhe={(r) => abrir('detalhe', r)}
            onEditar={(r) => abrir('editar', r)}
            onExcluir={(r) => abrir('excluir', r)}
          />
          <Paginacao
            pagina={pagina}
            totalPaginas={dados.totalPaginas}
            total={dados.total}
            onIr={irPara}
          />
        </>
      )}

      <ModalRegistro
        aberto={modal === 'criar' || modal === 'editar'}
        registro={modal === 'editar' ? (alvo ?? undefined) : undefined}
        enviando={acoes.enviando}
        erro={acoes.erro}
        onSalvar={salvar}
        onFechar={fechar}
      />
      <ModalDetalhes
        aberto={modal === 'detalhe'}
        registro={alvo}
        papel={papel}
        enviando={acoes.enviando}
        onValidar={validar}
        onFechar={fechar}
      />
      <ModalExclusao
        aberto={modal === 'excluir'}
        registro={alvo}
        enviando={acoes.enviando}
        erro={acoes.erro}
        onConfirmar={confirmarExclusao}
        onFechar={fechar}
      />
    </section>
  );
}
