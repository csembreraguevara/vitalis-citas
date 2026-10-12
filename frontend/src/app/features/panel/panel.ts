import { Component, computed, inject, OnInit, signal } from '@angular/core';

import { Rol, UsuarioActual } from '../../core/models/auth.models';
import { AuthService } from '../../core/services/auth.service';

interface Modulo {
  titulo: string;
  descripcion: string;
  requisito: string;
  roles: Rol[];
}

/** Módulos del sistema según el backlog; se habilitan en las siguientes entregas. */
const MODULOS: Modulo[] = [
  {
    titulo: 'Reservar cita',
    descripcion: 'Elige especialidad, médico y horario disponible.',
    requisito: 'RF008 · RF009',
    roles: ['PACIENTE', 'RECEPCIONISTA'],
  },
  {
    titulo: 'Mis citas',
    descripcion: 'Consulta, reprograma o cancela tus citas.',
    requisito: 'RF011 · RF012',
    roles: ['PACIENTE'],
  },
  {
    titulo: 'Agenda del día',
    descripcion: 'Citas programadas por médico y consultorio.',
    requisito: 'RF013',
    roles: ['RECEPCIONISTA', 'MEDICO', 'ADMIN'],
  },
  {
    titulo: 'Pacientes',
    descripcion: 'Registro y búsqueda de pacientes.',
    requisito: 'RF004',
    roles: ['RECEPCIONISTA', 'ADMIN'],
  },
  {
    titulo: 'Médicos y horarios',
    descripcion: 'Administración de médicos, especialidades y turnos.',
    requisito: 'RF005 · RF006 · RF007',
    roles: ['ADMIN'],
  },
  {
    titulo: 'Reportes',
    descripcion: 'Exportación de reportes de citas a Excel.',
    requisito: 'RF016 · RF017',
    roles: ['ADMIN'],
  },
];

const NOMBRE_ROL: Record<Rol, string> = {
  PACIENTE: 'Paciente',
  RECEPCIONISTA: 'Recepción',
  MEDICO: 'Médico',
  ADMIN: 'Administrador',
};

/** Panel principal después de iniciar sesión. */
@Component({
  selector: 'app-panel',
  templateUrl: './panel.html',
  styleUrl: './panel.scss',
})
export class Panel implements OnInit {
  protected readonly auth = inject(AuthService);

  protected readonly verificado = signal<UsuarioActual | null>(null);
  protected readonly errorVerificacion = signal(false);

  protected readonly nombreRol = computed(() => {
    const usuario = this.auth.usuario();
    return usuario ? NOMBRE_ROL[usuario.rol] : '';
  });

  protected readonly modulos = computed(() => {
    const rol = this.auth.usuario()?.rol;
    return rol ? MODULOS.filter((modulo) => modulo.roles.includes(rol)) : [];
  });

  ngOnInit(): void {
    // Llama a una ruta protegida para comprobar que el backend acepta el token.
    this.auth.usuarioActual().subscribe({
      next: (usuario) => this.verificado.set(usuario),
      error: () => this.errorVerificacion.set(true),
    });
  }
}
