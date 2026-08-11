/** Papéis de acesso (espelham o enum do backend). */
export type Papel = 'ADMIN' | 'SECRETARIA' | 'VISUALIZADOR';

export const ehAdmin = (papel?: string): boolean => papel === 'ADMIN';

/** Pode criar/editar/excluir/importar registros (ADMIN ou SECRETARIA). */
export const podeEscrever = (papel?: string): boolean =>
  papel === 'ADMIN' || papel === 'SECRETARIA';

/** Rótulo amigável do papel para exibição. */
export const rotuloPapel = (papel?: string): string => {
  switch (papel) {
    case 'ADMIN':
      return 'Administrador';
    case 'SECRETARIA':
      return 'Acesso completo';
    case 'VISUALIZADOR':
      return 'Visualização';
    default:
      return papel ?? '';
  }
};

export interface ItemNav {
  href: string;
  titulo: string;
  descricao: string;
  visivel: (papel?: string) => boolean;
}

/** Itens de navegação da área logada, filtrados por papel. */
export const itensNav: ItemNav[] = [
  {
    href: '/registros',
    titulo: 'Registros',
    descricao: 'Busque um aluno pelo nome ou RM e gerencie o índice.',
    visivel: () => true,
  },
  {
    href: '/importacao/planilha',
    titulo: 'Importar planilha',
    descricao: 'Carregue vários alunos de um arquivo .xlsx.',
    visivel: podeEscrever,
  },
  {
    href: '/importacao/imagem',
    titulo: 'Importar por foto',
    descricao: 'Fotografe uma página do livro e revise o texto reconhecido.',
    visivel: podeEscrever,
  },
  {
    href: '/usuarios',
    titulo: 'Usuários',
    descricao: 'Cadastre quem acessa o sistema e redefina senhas.',
    visivel: ehAdmin,
  },
  {
    href: '/auditoria',
    titulo: 'Auditoria',
    descricao: 'Veja o histórico de ações sensíveis.',
    visivel: ehAdmin,
  },
];

export const itensVisiveis = (papel?: string): ItemNav[] =>
  itensNav.filter((item) => item.visivel(papel));
