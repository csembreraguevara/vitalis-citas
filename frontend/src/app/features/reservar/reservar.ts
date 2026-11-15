import { Component, computed, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { catchError, finalize, merge, of, switchMap, tap } from 'rxjs';

import { mensajeDeError } from '../../core/http/mensaje-error';
import {
  Cita,
  CitaRequest,
  Disponibilidad,
  Especialidad,
  Medico,
  PacienteResumen,
} from '../../core/models/citas.models';
import { AuthService } from '../../core/services/auth.service';
import { CatalogoService } from '../../core/services/catalogo.service';
import { CitaService } from '../../core/services/cita.service';
import { fechaEnRango, fechaIso, sumarDias } from './validadores';

const DIAS_MAXIMOS = 60; // RN04

/**
 * Reserva de citas (RF008, RF009, RF010) con formulario reactivo y validaciones.
 * Paciente: la reserva queda PENDIENTE 5 minutos y debe confirmarla (RN02).
 * Recepción: busca al paciente por DNI y la cita queda CONFIRMADA de inmediato.
 */
@Component({
  selector: 'app-reservar',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './reservar.html',
  styleUrl: './reservar.scss',
})
export class Reservar implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly catalogo = inject(CatalogoService);
  private readonly citas = inject(CitaService);
  private readonly auth = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly esRecepcion = this.auth.usuario()?.rol === 'RECEPCIONISTA';
  protected readonly fechaMinima = fechaIso(new Date());
  protected readonly fechaMaxima = fechaIso(sumarDias(new Date(), DIAS_MAXIMOS));

  protected readonly form = this.fb.group({
    dni: ['', [Validators.required, Validators.pattern(/^\d{8}$/)]],
    idEspecialidad: this.fb.control<number | null>(null, Validators.required),
    idMedico: this.fb.control<number | null>(null, Validators.required),
    fecha: ['', [Validators.required, fechaEnRango(DIAS_MAXIMOS)]],
    hora: ['', Validators.required],
    motivo: ['', Validators.maxLength(250)],
  });

  // Datos que vienen de la API
  protected readonly especialidades = signal<Especialidad[]>([]);
  protected readonly medicos = signal<Medico[]>([]);
  protected readonly disponibilidad = signal<Disponibilidad | null>(null);
  protected readonly paciente = signal<PacienteResumen | null>(null);

  // Estados de carga y errores
  protected readonly cargandoMedicos = signal(false);
  protected readonly cargandoTurnos = signal(false);
  protected readonly buscandoPaciente = signal(false);
  protected readonly enviando = signal(false);
  protected readonly errorCatalogo = signal<string | null>(null);
  protected readonly errorTurnos = signal<string | null>(null);
  protected readonly errorPaciente = signal<string | null>(null);
  protected readonly errorEnvio = signal<string | null>(null);

  // Resultado de la reserva
  protected readonly reserva = signal<Cita | null>(null);
  protected readonly segundosRestantes = signal(0);
  private temporizador: ReturnType<typeof setInterval> | undefined;

  protected readonly tiempoRestante = computed(() => {
    const total = this.segundosRestantes();
    const minutos = Math.floor(total / 60);
    const segundos = String(total % 60).padStart(2, '0');
    return `${minutos}:${segundos}`;
  });

  protected readonly cantidadMotivo = signal(0);

  constructor() {
    if (!this.esRecepcion) {
      // El paciente reserva para sí mismo: el DNI no se pide (ni se valida).
      this.form.controls.dni.disable();
    }
    this.destroyRef.onDestroy(() => this.detenerTemporizador());
  }

  ngOnInit(): void {
    this.catalogo
      .especialidades()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (lista) => this.especialidades.set(lista),
        error: (error) => this.errorCatalogo.set(mensajeDeError(error)),
      });

    // Al cambiar la especialidad se vuelven a cargar sus médicos.
    this.form.controls.idEspecialidad.valueChanges
      .pipe(
        tap(() => {
          this.form.controls.idMedico.setValue(null);
          this.medicos.set([]);
        }),
        switchMap((id) => {
          if (id === null) {
            return of<Medico[]>([]);
          }
          this.cargandoMedicos.set(true);
          return this.catalogo.medicos(id).pipe(
            catchError((error) => {
              this.errorCatalogo.set(mensajeDeError(error));
              return of<Medico[]>([]);
            }),
            finalize(() => this.cargandoMedicos.set(false)),
          );
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((lista) => this.medicos.set(lista));

    // Con médico y fecha válidos se consultan los turnos.
    merge(this.form.controls.idMedico.valueChanges, this.form.controls.fecha.valueChanges)
      .pipe(
        tap(() => {
          this.form.controls.hora.setValue('');
          this.disponibilidad.set(null);
          this.errorTurnos.set(null);
        }),
        switchMap(() => {
          const { idMedico, fecha } = this.form.controls;
          if (idMedico.value === null || fecha.invalid) {
            return of(null);
          }
          this.cargandoTurnos.set(true);
          return this.catalogo.disponibilidad(idMedico.value, fecha.value).pipe(
            catchError((error) => {
              this.errorTurnos.set(mensajeDeError(error));
              return of(null);
            }),
            finalize(() => this.cargandoTurnos.set(false)),
          );
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((resultado) => this.disponibilidad.set(resultado));

    this.form.controls.dni.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => {
      this.paciente.set(null);
      this.errorPaciente.set(null);
    });

    this.form.controls.motivo.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((texto) => this.cantidadMotivo.set(texto.length));
  }

  /** true si el campo tiene error y el usuario ya lo tocó. */
  protected invalido(campo: keyof typeof this.form.controls): boolean {
    const control = this.form.controls[campo];
    return control.invalid && control.touched;
  }

  protected buscarPaciente(): void {
    const dni = this.form.controls.dni;
    dni.markAsTouched();
    if (dni.invalid) {
      return;
    }
    this.buscandoPaciente.set(true);
    this.citas
      .buscarPacientePorDni(dni.value)
      .pipe(finalize(() => this.buscandoPaciente.set(false)))
      .subscribe({
        next: (encontrado) => this.paciente.set(encontrado),
        error: (error) => this.errorPaciente.set(mensajeDeError(error)),
      });
  }

  protected elegirTurno(hora: string): void {
    this.form.controls.hora.setValue(hora);
    this.form.controls.hora.markAsTouched();
  }

  protected reservar(): void {
    this.errorEnvio.set(null);
    this.form.markAllAsTouched();
    if (this.esRecepcion && !this.paciente()) {
      this.errorPaciente.set('Busca al paciente por su DNI antes de reservar.');
    }
    if (this.form.invalid || (this.esRecepcion && !this.paciente())) {
      return;
    }

    const valores = this.form.getRawValue();
    const solicitud: CitaRequest = {
      idMedico: valores.idMedico!,
      fecha: valores.fecha,
      hora: valores.hora,
      motivo: valores.motivo.trim() || undefined,
      idPaciente: this.esRecepcion ? this.paciente()!.id : undefined,
    };

    this.enviando.set(true);
    this.citas
      .reservar(solicitud)
      .pipe(finalize(() => this.enviando.set(false)))
      .subscribe({
        next: (cita) => {
          this.reserva.set(cita);
          if (cita.estado === 'PENDIENTE' && cita.expiraEn) {
            this.iniciarTemporizador(cita.expiraEn);
          }
        },
        error: (error) => {
          this.errorEnvio.set(mensajeDeError(error));
          if (error.status === 409) {
            this.recargarTurnos(); // alguien tomó el turno: se muestra la disponibilidad actual
          }
        },
      });
  }

  protected confirmar(): void {
    const cita = this.reserva();
    if (!cita) {
      return;
    }
    this.errorEnvio.set(null);
    this.enviando.set(true);
    this.citas
      .confirmar(cita.id)
      .pipe(finalize(() => this.enviando.set(false)))
      .subscribe({
        next: (confirmada) => {
          this.detenerTemporizador();
          this.reserva.set(confirmada);
        },
        error: (error) => this.errorEnvio.set(mensajeDeError(error)),
      });
  }

  /** Vuelve al formulario conservando especialidad, médico y fecha. */
  protected elegirOtroHorario(): void {
    this.detenerTemporizador();
    this.reserva.set(null);
    this.errorEnvio.set(null);
    this.recargarTurnos();
  }

  protected nuevaReserva(): void {
    this.detenerTemporizador();
    this.reserva.set(null);
    this.errorEnvio.set(null);
    this.paciente.set(null);
    this.form.reset();
  }

  protected fechaLarga(fecha: string): string {
    const texto = new Intl.DateTimeFormat('es-PE', {
      weekday: 'long',
      day: 'numeric',
      month: 'long',
      year: 'numeric',
    }).format(new Date(`${fecha}T00:00:00`));
    return texto.charAt(0).toUpperCase() + texto.slice(1); // "Lunes, 16 de noviembre de 2026"
  }

  protected horaCorta(hora: string): string {
    return hora.slice(0, 5);
  }

  private recargarTurnos(): void {
    // Volver a emitir la fecha dispara la consulta de disponibilidad.
    this.form.controls.fecha.setValue(this.form.controls.fecha.value);
  }

  private iniciarTemporizador(expiraEn: string): void {
    // El backend envía la hora local de Lima sin zona: "2026-11-16T10:05:00.123".
    const limite = new Date(expiraEn.slice(0, 19)).getTime();
    const actualizar = () => {
      const restantes = Math.max(0, Math.round((limite - Date.now()) / 1000));
      this.segundosRestantes.set(restantes);
      if (restantes === 0) {
        this.detenerTemporizador();
      }
    };
    this.detenerTemporizador();
    actualizar();
    this.temporizador = setInterval(actualizar, 1000);
  }

  private detenerTemporizador(): void {
    if (this.temporizador) {
      clearInterval(this.temporizador);
      this.temporizador = undefined;
    }
  }
}
