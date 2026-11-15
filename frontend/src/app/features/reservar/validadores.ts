import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/** Fecha local en formato "yyyy-MM-dd" (el mismo que usa <input type="date">). */
export function fechaIso(fecha: Date): string {
  const mes = String(fecha.getMonth() + 1).padStart(2, '0');
  const dia = String(fecha.getDate()).padStart(2, '0');
  return `${fecha.getFullYear()}-${mes}-${dia}`;
}

export function sumarDias(fecha: Date, dias: number): Date {
  const copia = new Date(fecha);
  copia.setDate(copia.getDate() + dias);
  return copia;
}

/**
 * Validador personalizado (RN04): la fecha no puede ser pasada ni estar a más
 * de `diasMaximos` días. Devuelve { fechaPasada } o { fechaLejana }.
 */
export function fechaEnRango(diasMaximos: number): ValidatorFn {
  return (control: AbstractControl<string>): ValidationErrors | null => {
    const valor = control.value;
    if (!valor) {
      return null; // de eso se encarga Validators.required
    }
    const hoy = fechaIso(new Date());
    const limite = fechaIso(sumarDias(new Date(), diasMaximos));
    if (valor < hoy) {
      return { fechaPasada: true };
    }
    if (valor > limite) {
      return { fechaLejana: { maximo: limite } };
    }
    return null;
  };
}
