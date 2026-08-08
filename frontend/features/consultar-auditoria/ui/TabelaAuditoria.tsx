import type { RegistroAuditoria } from '@/entities/auditoria/model/types';

export function TabelaAuditoria({ itens }: { itens: RegistroAuditoria[] }) {
  if (itens.length === 0) {
    return <p>Nenhum evento de auditoria.</p>;
  }

  return (
    <table>
      <caption>Histórico de ações</caption>
      <thead>
        <tr>
          <th scope="col">Quando</th>
          <th scope="col">Usuário</th>
          <th scope="col">Ação</th>
          <th scope="col">Entidade</th>
          <th scope="col">Detalhe</th>
        </tr>
      </thead>
      <tbody>
        {itens.map((a) => (
          <tr key={a.id}>
            <td>{new Date(a.criadoEm).toLocaleString('pt-BR')}</td>
            <td>{a.usuario}</td>
            <td>{a.acao}</td>
            <td>
              {a.entidade}
              {a.entidadeId ? ` #${a.entidadeId}` : ''}
            </td>
            <td>{a.detalhe ?? '—'}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
