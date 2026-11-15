import { Component, computed, ElementRef, inject, OnInit, signal, viewChild } from '@angular/core';
import {
  AbstractControl,
  NonNullableFormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { finalize, forkJoin } from 'rxjs';

import { ESTADO_CITA, fechaLarga, horaCorta } from '../../core/formato';
import { mensajeDeError } from '../../core/http/mensaje-error';
import { Cita } from '../../core/models/citas.models';
import {
  Conteos,
  FilaOcupacion,
  FilaOcupacionMedico,
  ReporteService,
} from '../../core/services/reporte.service';
import { fechaIso } from '../reservar/validadores';

type EstadoVista = 'inicial' | 'cargando' | 'listo' | 'error';

/** Validador de grupo: la fecha final no puede ser anterior a la inicial. */
function rangoValido(grupo: AbstractControl): ValidationErrors | null {
  const { desde, hasta } = grupo.value as { desde: string; hasta: string };
  return desde && hasta && hasta < desde ? { rangoInvertido: true } : null;
}

/**
 * Reporte R01 de ocupación (RF016). Solo administrador.
 * Tres niveles: especialidad → médicos (se despliegan) → citas del médico.
 */
@Component({
  selector: 'app-reportes',
  imports: [ReactiveFormsModule],
  templateUrl: './reportes.html',
  styleUrl: './reportes.scss',
})
export class Reportes implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly reporteService = inject(ReporteService);
  private readonly panelDetalle = viewChild<ElementRef<HTMLElement>>('panelDetalle');

  protected readonly form = this.fb.group(
    {
      desde: ['', Validators.required],
      hasta: ['', Validators.required],
    },
    { validators: rangoValido },
  );

  protected readonly estadoVista = signal<EstadoVista>('inicial');
  protected readonly error = signal('');
  protected readonly filas = signal<FilaOcupacion[]>([]);
  protected readonly filasMedico = signal<FilaOcupacionMedico[]>([]);
  protected readonly periodo = signal('');
  /** Rango de la última consulta (el detalle usa el mismo aunque cambien los filtros). */
  private rango = { desde: '', hasta: '' };

  // Nivel 2: especialidades desplegadas
  protected readonly desplegadas = signal<ReadonlySet<string>>(new Set());

  // Nivel 3: citas del médico elegido
  protected readonly medicoElegido = signal<FilaOcupacionMedico | null>(null);
  protected readonly estadoDetalle = signal<EstadoVista>('inicial');
  protected readonly errorDetalle = signal('');
  protected readonly citasDetalle = signal<Cita[]>([]);

  protected readonly totales = computed(() =>
    this.filas().reduce(
      (suma, fila) => ({
        pendientes: suma.pendientes + fila.pendientes,
        confirmadas: suma.confirmadas + fila.confirmadas,
        atendidas: suma.atendidas + fila.atendidas,
        noAsistio: suma.noAsistio + fila.noAsistio,
        canceladas: suma.canceladas + fila.canceladas,
        total: suma.total + fila.total,
      }),
      { pendientes: 0, confirmadas: 0, atendidas: 0, noAsistio: 0, canceladas: 0, total: 0 },
    ),
  );

  protected readonly estado = ESTADO_CITA;
  protected readonly fechaLarga = fechaLarga;
  protected readonly horaCorta = horaCorta;

  ngOnInit(): void {
    // Por defecto: el mes actual completo.
    const hoy = new Date();
    this.form.setValue({
      desde: fechaIso(new Date(hoy.getFullYear(), hoy.getMonth(), 1)),
      hasta: fechaIso(new Date(hoy.getFullYear(), hoy.getMonth() + 1, 0)),
    });
    this.consultar();
  }

  protected consultar(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid) {
      return;
    }
    const { desde, hasta } = this.form.getRawValue();
    this.estadoVista.set('cargando');
    this.cerrarDetalle();

    // Las dos consultas viajan en paralelo; la vista se muestra cuando llegan ambas.
    forkJoin({
      especialidades: this.reporteService.ocupacion(desde, hasta),
      medicos: this.reporteService.ocupacionPorMedico(desde, hasta),
    }).subscribe({
      next: ({ especialidades, medicos }) => {
        this.rango = { desde, hasta };
        this.periodo.set(`${fechaLarga(desde)} al ${fechaLarga(hasta)}`);
        this.filas.set(especialidades);
        this.filasMedico.set(medicos);
        this.desplegadas.set(new Set());
        this.estadoVista.set('listo');
      },
      error: (error) => {
        this.error.set(mensajeDeError(error));
        this.estadoVista.set('error');
      },
    });
  }

  protected medicosDe(especialidad: string): FilaOcupacionMedico[] {
    return this.filasMedico().filter((fila) => fila.especialidad === especialidad);
  }

  protected estaDesplegada(especialidad: string): boolean {
    return this.desplegadas().has(especialidad);
  }

  protected alternar(especialidad: string): void {
    this.desplegadas.update((actual) => {
      const nuevo = new Set(actual);
      if (nuevo.has(especialidad)) {
        nuevo.delete(especialidad);
      } else {
        nuevo.add(especialidad);
      }
      return nuevo;
    });
  }

  protected readonly exportando = signal(false);
  protected readonly errorExportar = signal<string | null>(null);

  /** Descarga el Excel del periodo consultado (las cifras coinciden con la tabla). */
  protected exportarExcel(): void {
    const { desde, hasta } = this.rango;
    this.exportando.set(true);
    this.errorExportar.set(null);
    this.reporteService
      .ocupacionExcel(desde, hasta)
      .pipe(finalize(() => this.exportando.set(false)))
      .subscribe({
        next: (archivo) => {
          const url = URL.createObjectURL(archivo);
          const enlace = document.createElement('a');
          enlace.href = url;
          enlace.download = `ocupacion_${desde}_${hasta}.xlsx`;
          enlace.click();
          URL.revokeObjectURL(url);
        },
        error: (error) => this.errorExportar.set(mensajeDeError(error)),
      });
  }

  protected verCitas(medico: FilaOcupacionMedico): void {
    this.medicoElegido.set(medico);
    this.estadoDetalle.set('cargando');
    this.reporteService
      .citasDelMedico(medico.idMedico, this.rango.desde, this.rango.hasta)
      .subscribe({
        next: (citas) => {
          this.citasDetalle.set(citas);
          this.estadoDetalle.set('listo');
          // Lleva la vista al panel para que se note que apareció.
          setTimeout(() =>
            this.panelDetalle()?.nativeElement.scrollIntoView({
              behavior: 'smooth',
              block: 'start',
            }),
          );
        },
        error: (error) => {
          this.errorDetalle.set(mensajeDeError(error));
          this.estadoDetalle.set('error');
        },
      });
  }

  protected cerrarDetalle(): void {
    this.medicoElegido.set(null);
    this.citasDetalle.set([]);
    this.estadoDetalle.set('inicial');
  }

  /** Porcentaje de citas atendidas sobre las que debían atenderse (sin pendientes ni canceladas). */
  protected asistencia(fila: Conteos): string {
    const base = fila.atendidas + fila.noAsistio;
    return base === 0 ? '—' : `${Math.round((fila.atendidas / base) * 100)} %`;
  }
}
