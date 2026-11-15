import { Rol } from './models/auth.models';

/** Una opción del menú lateral y de las tarjetas del panel. */
export interface OpcionMenu {
  ruta: string;
  titulo: string;
  descripcion: string;
  requisito: string;
  roles: Rol[];
}

/**
 * Opciones del sistema por rol (RF003). El menú solo muestra lo que el rol puede usar,
 * pero quien decide de verdad es el backend: si alguien entra igual, la API responde 403.
 */
export const OPCIONES_MENU: OpcionMenu[] = [
  {
    ruta: '/agenda',
    titulo: 'Agenda del día',
    descripcion: 'Citas del día por médico y registro de asistencia.',
    requisito: 'RF013',
    roles: ['RECEPCIONISTA', 'MEDICO', 'ADMIN'],
  },
  {
    ruta: '/reservar',
    titulo: 'Reservar cita',
    descripcion: 'Elige especialidad, médico y horario disponible.',
    requisito: 'RF008 · RF009',
    roles: ['PACIENTE', 'RECEPCIONISTA'],
  },
  {
    ruta: '/mis-citas',
    titulo: 'Mis citas',
    descripcion: 'Consulta o cancela tus citas.',
    requisito: 'RF011 · RF012',
    roles: ['PACIENTE'],
  },
  {
    ruta: '/reportes',
    titulo: 'Reportes',
    descripcion: 'Ocupación de citas por especialidad.',
    requisito: 'RF016',
    roles: ['ADMIN'],
  },
];

export function opcionesPara(rol: Rol | undefined): OpcionMenu[] {
  return rol ? OPCIONES_MENU.filter((opcion) => opcion.roles.includes(rol)) : [];
}

export const NOMBRE_ROL: Record<Rol, string> = {
  PACIENTE: 'Paciente',
  RECEPCIONISTA: 'Recepción',
  MEDICO: 'Médico',
  ADMIN: 'Administrador',
};
