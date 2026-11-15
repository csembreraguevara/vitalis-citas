/** Modelos de la API de citas (mismos nombres que los DTO del backend). */

export interface Especialidad {
  id: number;
  nombre: string;
  descripcion: string;
  duracionCitaMin: number;
}

export interface Medico {
  id: number;
  nombreCompleto: string;
  idEspecialidad: number;
  especialidad: string;
  consultorio: string;
}

/** Hora en formato "HH:mm:ss", tal como la envía el backend. */
export interface Turno {
  hora: string;
  disponible: boolean;
}

export interface Disponibilidad {
  idMedico: number;
  medico: string;
  especialidad: string;
  consultorio: string;
  fecha: string;
  duracionMin: number;
  turnos: Turno[];
}

export type EstadoCita =
  'PENDIENTE' | 'CONFIRMADA' | 'ATENDIDA' | 'CANCELADA' | 'NO_ASISTIO' | 'EXPIRADA';

export interface Cita {
  id: number;
  fecha: string;
  hora: string;
  estado: EstadoCita;
  canal: 'PORTAL' | 'RECEPCION';
  idMedico: number;
  medico: string;
  especialidad: string;
  consultorio: string;
  paciente: string;
  motivo: string | null;
  /** Solo en reservas PENDIENTE: hasta cuándo hay que confirmarla (RN02). */
  expiraEn: string | null;
}

/** Cuerpo de POST /api/citas. idPaciente solo lo envía recepción. */
export interface CitaRequest {
  idMedico: number;
  fecha: string;
  hora: string;
  motivo?: string;
  idPaciente?: number;
}

export interface PacienteResumen {
  id: number;
  dni: string;
  nombreCompleto: string;
}
