import { EstadoCita } from './models/citas.models';

/** "2026-11-16" → "Lunes, 16 de noviembre de 2026" */
export function fechaLarga(fecha: string): string {
  const texto = new Intl.DateTimeFormat('es-PE', {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  }).format(new Date(`${fecha}T00:00:00`));
  return texto.charAt(0).toUpperCase() + texto.slice(1);
}

/** "09:00:00" → "09:00" */
export function horaCorta(hora: string): string {
  return hora.slice(0, 5);
}

/** Texto y color (clase de Bootstrap) de cada estado de la cita. */
export const ESTADO_CITA: Record<EstadoCita, { texto: string; clase: string }> = {
  PENDIENTE: { texto: 'Por confirmar', clase: 'text-bg-warning' },
  CONFIRMADA: { texto: 'Confirmada', clase: 'text-bg-success' },
  ATENDIDA: { texto: 'Atendida', clase: 'text-bg-secondary' },
  CANCELADA: { texto: 'Cancelada', clase: 'text-bg-light border' },
  NO_ASISTIO: { texto: 'No asistió', clase: 'text-bg-light border' },
  EXPIRADA: { texto: 'Expirada', clase: 'text-bg-light border' },
};
