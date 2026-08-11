'use client';

import { useState, type FormEvent } from 'react';

import type { AlteracaoUsuario, UsuarioResumo } from '@/entities/usuario/model/types';
import type { Papel } from '@/shared/auth/roles';
import { Modal } from '@/shared/ui/Modal';

const PAPEIS: { valor: Papel; rotulo: string }[] = [
  { valor: 'VISUALIZADOR', rotulo: 'Visualização (só consulta)' },
  { valor: 'SECRETARIA', rotulo: 'Acesso completo (CRUD de alunos)' },
  { valor: 'ADMIN', rotulo: 'Administrador (tudo + usuários)' },
];

interface Props {
  aberto: boolean;
  usuario: UsuarioResumo | null;
  enviando: boolean;
  erro: string | null;
  onSalvar: (id: number, dados: AlteracaoUsuario) => void;
  onFechar: () => void;
}

/** Form interno: montado só quando o modal abre, então o estado inicia do usuário atual. */
function FormEditar({ usuario, enviando, erro, onSalvar, onFechar }: Omit<Props, 'aberto'> & { usuario: UsuarioResumo }) {
  const [nome, setNome] = useState(usuario.nome);
  const [papel, setPapel] = useState<Papel>(usuario.papel as Papel);

  function onSubmit(e: FormEvent) {
    e.preventDefault();
    if (nome.trim()) {
      onSalvar(usuario.id, { nome: nome.trim(), papel });
    }
  }

  return (
    <form onSubmit={onSubmit} aria-label="Editar usuário">
      <label htmlFor="edit-username">Usuário (login)</label>
      <input id="edit-username" value={usuario.username} readOnly disabled />

      <label htmlFor="edit-nome">Nome</label>
      <input id="edit-nome" value={nome} onChange={(e) => setNome(e.target.value)} required />

      <label htmlFor="edit-papel">Papel</label>
      <select id="edit-papel" value={papel} onChange={(e) => setPapel(e.target.value as Papel)}>
        {PAPEIS.map((p) => (
          <option key={p.valor} value={p.valor}>
            {p.rotulo}
          </option>
        ))}
      </select>

      {erro && <p role="alert">{erro}</p>}

      <div className="modal__acoes">
        <button type="submit" disabled={enviando}>
          {enviando ? 'Salvando…' : 'Salvar alterações'}
        </button>
        <button type="button" className="btn-secundario" onClick={onFechar} disabled={enviando}>
          Cancelar
        </button>
      </div>
    </form>
  );
}

export function ModalEditarUsuario({ aberto, usuario, enviando, erro, onSalvar, onFechar }: Props) {
  return (
    <Modal titulo="Editar usuário" aberto={aberto} onFechar={onFechar}>
      {usuario && (
        <FormEditar
          usuario={usuario}
          enviando={enviando}
          erro={erro}
          onSalvar={onSalvar}
          onFechar={onFechar}
        />
      )}
    </Modal>
  );
}
