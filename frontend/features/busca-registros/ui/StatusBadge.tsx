import type { StatusValidacao } from '@/entities/registro/model/types';

/** Selo de status: ponto colorido + rótulo (Pendente âmbar, Validado verde). */
export function StatusBadge({ status }: { status: StatusValidacao }) {
  const validado = status === 'VALIDADO';
  return (
    <span className={`selo ${validado ? 'selo--ok' : 'selo--pendente'}`}>
      <span className="selo__ponto" aria-hidden />
      {validado ? 'Validado' : 'Pendente'}
    </span>
  );
}
