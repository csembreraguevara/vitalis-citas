import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize, Observable } from 'rxjs';

import { ESTADO_CITA, fechaLarga, horaCorta } from '../../core/formato';
import { mensajeDeError } from '../../core/http/mensaje-error';
import { Cita, Medico } from '../../core/models/citas.models';
import { AgendaService } from '../../core/services/agenda.service';
import { AuthService } from '../../core/services/auth.service';
import { fechaIso, sumarDias } from '../reservar/validadores';

type EstadoVista = 'cargando' | 'listo' | 'error';

/**
 * Agenda del día (RF013). Recepción y administración ven a todos los médicos
 * (con filtro); el médico ve solo su agenda. Recepción y médico registran la asistencia.
 */
@Component({
  selector: 'app-agenda',
  imports: [FormsModule],
  templateUrl: './agenda.html',
  styleUrl: './agenda.scss',
})
export class Agenda implements OnInit {
  private readonly agendaService = inject(AgendaService);
  private readonly rol = inject(AuthService).usuario()?.rol;

  protected readonly esMedico = this.rol === 'MEDICO';
  /** El administrador consulta; quien registra la asistencia es recepción o el médico. */
  protected readonly puedeRegistrar = this.rol === 'MEDICO' || this.rol === 'RECEPCIONISTA';
  protected readonly hoy = fechaIso(new Date());

  protected readonly fecha = signal(this.hoy);
  protected readonly idMedico = signal<number | null>(null);
  protected readonly medicos = signal<Medico[]>([]);

  protected readonly estadoVista = signal<EstadoVista>('cargando');
  protected readonly error = signal('');
  protected readonly citas = signal<Cita[]>([]);
  protected readonly procesandoId = signal<number | null>(null);
  protected readonly errorAccion = signal<string | null>(null);

  protected readonly resumen = computed(() => {
    const lista = this.citas();
    const contar = (estado: Cita['estado']) => lista.filter((c) => c.estado === estado).length;
    return {
      total: lista.length,
      pendientes: contar('CONFIRMADA') + contar('PENDIENTE'),
      atendidas: contar('ATENDIDA'),
      noAsistio: contar('NO_ASISTIO'),
    };
  });

  protected readonly esFutura = computed(() => this.fecha() > this.hoy);

  protected readonly estado = ESTADO_CITA;
  protected readonly fechaLarga = fechaLarga;
  protected readonly horaCorta = horaCorta;

  ngOnInit(): void {
    if (!this.esMedico) {
      this.agendaService.medicos().subscribe({
        next: (lista) => this.medicos.set(lista),
        error: () => this.medicos.set([]), // el filtro es opcional: la agenda funciona sin él
      });
    }
    this.cargar();
  }

  protected cargar(): void {
    this.estadoVista.set('cargando');
    this.errorAccion.set(null);
    this.agendaService.citasDelDia(this.fecha(), this.idMedico()).subscribe({
      next: (lista) => {
        this.citas.set(lista);
        this.estadoVista.set('listo');
      },
      error: (error) => {
        this.error.set(mensajeDeError(error));
        this.estadoVista.set('error');
      },
    });
  }

  protected cambiarFecha(fecha: string): void {
    if (fecha) {
      this.fecha.set(fecha);
      this.cargar();
    }
  }

  protected moverDia(dias: number): void {
    this.cambiarFecha(fechaIso(sumarDias(new Date(`${this.fecha()}T00:00:00`), dias)));
  }

  protected cambiarMedico(id: number | null): void {
    this.idMedico.set(id);
    this.cargar();
  }

  protected atendida(cita: Cita): void {
    this.registrar(cita, this.agendaService.marcarAtendida(cita.id));
  }

  protected noAsistio(cita: Cita): void {
    this.registrar(cita, this.agendaService.marcarNoAsistio(cita.id));
  }

  private registrar(cita: Cita, peticion: Observable<Cita>): void {
    this.procesandoId.set(cita.id);
    this.errorAccion.set(null);
    peticion.pipe(finalize(() => this.procesandoId.set(null))).subscribe({
      next: (actualizada) =>
        this.citas.update((lista) => lista.map((c) => (c.id === actualizada.id ? actualizada : c))),
      error: (error) => this.errorAccion.set(mensajeDeError(error)),
    });
  }
}
