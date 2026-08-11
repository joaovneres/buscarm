'use client';

import { useState } from 'react';

import type { AlteracaoUsuario, NovoUsuario, UsuarioResumo } from '@/entities/usuario/model/types';
import { rotuloPapel } from '@/shared/auth/roles';
import { Modal } from '@/shared/ui/Modal';

import { useGestaoUsuarios } from '../model/useGestaoUsuarios';
import { FormularioUsuario } from './FormularioUsuario';
import { ModalEditarUsuario } from './ModalEditarUsuario';
import { ModalResetSenha } from './ModalResetSenha';

export function ListaUsuarios() {
  const g = useGestaoUsuarios();
  const [modal, setModal] = useState<'criar' | 'editar' | 'reset' | null>(null);
  const [alvo, setAlvo] = useState<UsuarioResumo | null>(null);

  function abrir(tipo: 'criar' | 'editar' | 'reset', usuario: UsuarioResumo | null) {
    g.limparErroAcao();
    setAlvo(usuario);
    setModal(tipo);
  }

  async function criar(dados: NovoUsuario) {
    if (await g.criar(dados)) {
      setModal(null);
    }
  }

  async function editar(id: number, dados: AlteracaoUsuario) {
    if (await g.alterar(id, dados)) {
      setModal(null);
    }
  }

  async function resetar(id: number, senha: string) {
    if (await g.resetar(id, senha)) {
      setModal(null);
    }
  }

  return (
    <section className="lista" aria-label="Usuários do sistema">
      <div className="lista__topo">
        <button type="button" onClick={() => abrir('criar', null)}>
          + Novo usuário
        </button>
      </div>

      {g.estado === 'loading' && <p role="status">Carregando…</p>}
      {g.estado === 'erro' && <p role="alert">{g.erro}</p>}

      {g.estado === 'sucesso' && (
        <div className="tabela-wrap">
          <table className="tabela-registros">
            <thead>
              <tr>
                <th scope="col">Nome</th>
                <th scope="col">Usuário</th>
                <th scope="col">Papel</th>
                <th scope="col" className="col-acoes">
                  Ações
                </th>
              </tr>
            </thead>
            <tbody>
              {g.lista.map((u) => (
                <tr key={u.id}>
                  <td className="col-nome">{u.nome}</td>
                  <td>
                    <span className="rm">{u.username}</span>
                  </td>
                  <td>{rotuloPapel(u.papel)}</td>
                  <td className="col-acoes">
                    <div className="acoes">
                      <button type="button" className="btn-secundario" onClick={() => abrir('editar', u)}>
                        Editar
                      </button>
                      <button type="button" className="btn-secundario" onClick={() => abrir('reset', u)}>
                        Redefinir senha
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <Modal titulo="Novo usuário" aberto={modal === 'criar'} onFechar={() => setModal(null)}>
        <FormularioUsuario
          enviando={g.enviando}
          erro={g.erroAcao}
          onSalvar={criar}
          onCancelar={() => setModal(null)}
        />
      </Modal>

      <ModalEditarUsuario
        aberto={modal === 'editar'}
        usuario={alvo}
        enviando={g.enviando}
        erro={g.erroAcao}
        onSalvar={editar}
        onFechar={() => setModal(null)}
      />

      <ModalResetSenha
        aberto={modal === 'reset'}
        usuario={alvo}
        enviando={g.enviando}
        erro={g.erroAcao}
        onConfirmar={resetar}
        onFechar={() => setModal(null)}
      />
    </section>
  );
}
