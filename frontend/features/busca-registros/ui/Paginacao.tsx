'use client';

interface Props {
  pagina: number;
  totalPaginas: number;
  total: number;
  onIr: (pagina: number) => void;
}

/** Navegação entre páginas (20 por página). */
export function Paginacao({ pagina, totalPaginas, total, onIr }: Props) {
  if (total === 0) {
    return null;
  }
  return (
    <nav className="paginacao" aria-label="Paginação">
      <button
        type="button"
        className="btn-secundario"
        onClick={() => onIr(pagina - 1)}
        disabled={pagina <= 0}
      >
        ← Anterior
      </button>
      <span className="paginacao__info">
        Página {pagina + 1} de {Math.max(totalPaginas, 1)} · {total}{' '}
        {total === 1 ? 'registro' : 'registros'}
      </span>
      <button
        type="button"
        className="btn-secundario"
        onClick={() => onIr(pagina + 1)}
        disabled={pagina + 1 >= totalPaginas}
      >
        Próxima →
      </button>
    </nav>
  );
}
