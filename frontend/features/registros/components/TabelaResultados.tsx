import type { RegistroResponse } from '../types';

export function TabelaResultados({ registros }: { registros: RegistroResponse[] }) {
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
        </tr>
      </thead>
      <tbody>
        {registros.map((r) => (
          <tr key={r.id}>
            <td>
              <strong>{r.rm}</strong>
            </td>
            <td>{r.nomeCompleto}</td>
            <td>{r.anoRegistro ?? '—'}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
