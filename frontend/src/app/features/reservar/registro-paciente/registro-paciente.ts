import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, input, OnInit, output, signal } from '@angular/core';
import {
  AbstractControl,
  NonNullableFormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { finalize } from 'rxjs';

import { mensajeDeError } from '../../../core/http/mensaje-error';
import { ErrorResponse } from '../../../core/models/auth.models';
import { PacienteResumen } from '../../../core/models/citas.models';
import { PacienteService } from '../../../core/services/paciente.service';
import { fechaIso } from '../validadores';

/** La fecha de nacimiento no puede ser futura. */
function noFutura(control: AbstractControl<string>): ValidationErrors | null {
  return control.value && control.value > fechaIso(new Date()) ? { futura: true } : null;
}

/**
 * Formulario para registrar un paciente nuevo desde recepción (RF004).
 * Comunicación entre componentes (S12):
 * - Entrada `dni`: el DNI que recepción ya escribió en la reserva.
 * - Salida `registrado`: avisa a la pantalla de reserva con el paciente creado.
 * - Salida `cancelado`: cierra el formulario sin registrar.
 */
@Component({
  selector: 'app-registro-paciente',
  imports: [ReactiveFormsModule],
  templateUrl: './registro-paciente.html',
  styleUrl: './registro-paciente.scss',
})
export class RegistroPaciente implements OnInit {
  readonly dni = input.required<string>();
  readonly registrado = output<PacienteResumen>();
  readonly cancelado = output<void>();

  private readonly fb = inject(NonNullableFormBuilder);
  private readonly pacienteService = inject(PacienteService);

  protected readonly hoy = fechaIso(new Date());
  protected readonly enviando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly detalles = signal<string[]>([]);

  protected readonly form = this.fb.group({
    nombres: ['', [Validators.required, Validators.maxLength(80)]],
    apellidos: ['', [Validators.required, Validators.maxLength(80)]],
    fechaNacimiento: ['', [Validators.required, noFutura]],
    sexo: ['' as '' | 'F' | 'M', Validators.required],
    telefono: ['', Validators.pattern(/^9\d{8}$/)],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(120)]],
  });

  ngOnInit(): void {
    // Enfoca el primer campo para que recepción escriba de inmediato.
    setTimeout(() => document.getElementById('reg-nombres')?.focus());
  }

  protected invalido(campo: keyof typeof this.form.controls): boolean {
    const control = this.form.controls[campo];
    return control.invalid && control.touched;
  }

  protected guardar(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid) {
      return;
    }
    const valores = this.form.getRawValue();
    this.enviando.set(true);
    this.error.set(null);
    this.detalles.set([]);
    this.pacienteService
      .registrar({
        dni: this.dni(),
        nombres: valores.nombres,
        apellidos: valores.apellidos,
        fechaNacimiento: valores.fechaNacimiento,
        sexo: valores.sexo as 'F' | 'M',
        telefono: valores.telefono || undefined,
        email: valores.email,
      })
      .pipe(finalize(() => this.enviando.set(false)))
      .subscribe({
        next: (paciente) => this.registrado.emit(paciente),
        error: (error) => {
          this.error.set(mensajeDeError(error));
          if (error instanceof HttpErrorResponse) {
            this.detalles.set((error.error as Partial<ErrorResponse> | null)?.detalles ?? []);
          }
        },
      });
  }
}
