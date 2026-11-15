import { Component, computed, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { ESTADO_CITA, fechaLarga, horaCorta } from '../../core/formato';
import { mensajeDeError } from '../../core/http/mensaje-error';
import { Cita } from '../../core/models/citas.models';
import { CitaService } from '../../core/services/cita.service';

type EstadoVista = 'cargando' | 'listo' | 'error';

const HORAS_MINIMAS_CANCELACION = 2; // RN03

/**
 * Citas del paciente (RF011, RF012). Muestra los cuatro estados de la vista:
 * cargando, con datos, sin resultados y error (con opción de reintentar).
 */
@Component({
  selector: 'app-mis-citas',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './mis-citas.html',
  styleUrl: './mis-citas.scss',
})
export class MisCitas implements OnInit {
  private readonly citaService = inject(CitaService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly estadoVista = signal<EstadoVista>('cargando');
  protected readonly errorCarga = signal('');
  protected readonly citas = signal<Cita[]>([]);

  protected readonly proximas = computed(() =>
    this.citas()
      .filter((cita) => this.esProxima(cita))
      .sort((a, b) => (a.fecha + a.hora).localeCompare(b.fecha + b.hora)),
  );
  protected readonly historial = computed(() =>
    this.citas().filter((cita) => !this.esProxima(cita)),
  );

  // Cancelación (una cita a la vez)
  protected readonly cancelandoId = signal<number | null>(null);
  protected readonly enviando = signal(false);
  protected readonly errorAccion = signal<string | null>(null);
  protected readonly aviso = signal<string | null>(null);
  protected readonly motivo = new FormControl('', {
    nonNullable: true,
    validators: [Validators.required, Validators.maxLength(250)],
  });

  protected readonly estado = ESTADO_CITA;
  protected readonly fechaLarga = fechaLarga;
  protected readonly horaCorta = horaCorta;

  ngOnInit(): void {
    this.cargar();
  }

  protected cargar(): void {
    this.estadoVista.set('cargando');
    this.citaService
      .misCitas()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (lista) => {
          this.citas.set(lista);
          this.estadoVista.set('listo');
        },
        error: (error) => {
          this.errorCarga.set(mensajeDeError(error));
          this.estadoVista.set('error');
        },
      });
  }

  /** RN03: se puede cancelar hasta 2 horas antes. El backend lo vuelve a validar. */
  protected puedeCancelar(cita: Cita): boolean {
    const limite = this.fechaHora(cita).getTime() - HORAS_MINIMAS_CANCELACION * 3_600_000;
    return Date.now() < limite;
  }

  protected abrirCancelacion(cita: Cita): void {
    this.cancelandoId.set(cita.id);
    this.motivo.reset();
    this.errorAccion.set(null);
    this.aviso.set(null);
  }

  protected cerrarCancelacion(): void {
    this.cancelandoId.set(null);
    this.errorAccion.set(null);
  }

  protected cancelar(cita: Cita): void {
    this.motivo.markAsTouched();
    if (this.motivo.invalid) {
      return;
    }
    this.enviando.set(true);
    this.errorAccion.set(null);
    this.citaService
      .cancelar(cita.id, this.motivo.value.trim())
      .pipe(finalize(() => this.enviando.set(false)))
      .subscribe({
        next: (actualizada) => {
          this.reemplazar(actualizada);
          this.cancelandoId.set(null);
          this.aviso.set(`Cancelaste tu cita del ${fechaLarga(cita.fecha).toLowerCase()}.`);
        },
        error: (error) => this.errorAccion.set(mensajeDeError(error)),
      });
  }

  /** Para las reservas que quedaron PENDIENTE (RN02). */
  protected confirmar(cita: Cita): void {
    this.enviando.set(true);
    this.errorAccion.set(null);
    this.aviso.set(null);
    this.citaService
      .confirmar(cita.id)
      .pipe(finalize(() => this.enviando.set(false)))
      .subscribe({
        next: (actualizada) => this.reemplazar(actualizada),
        error: (error) => {
          this.errorAccion.set(mensajeDeError(error));
          this.cargar(); // si venció, se muestra como expirada
        },
      });
  }

  private reemplazar(actualizada: Cita): void {
    this.citas.update((lista) =>
      lista.map((cita) => (cita.id === actualizada.id ? actualizada : cita)),
    );
  }

  private esProxima(cita: Cita): boolean {
    const activa = cita.estado === 'PENDIENTE' || cita.estado === 'CONFIRMADA';
    return activa && this.fechaHora(cita).getTime() > Date.now();
  }

  private fechaHora(cita: Cita): Date {
    return new Date(`${cita.fecha}T${cita.hora.slice(0, 8)}`);
  }
}
