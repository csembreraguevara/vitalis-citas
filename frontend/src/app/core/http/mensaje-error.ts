import { HttpErrorResponse } from '@angular/common/http';

import { ErrorResponse } from '../models/auth.models';

/**
 * Traduce un error HTTP a un mensaje para el usuario.
 * - 0: el backend no responde.
 * - 401: la sesión venció (el interceptor ya cierra la sesión).
 * - 403: el rol no tiene permiso.
 * - 400/404/409/422: el backend explica el motivo en ErrorResponseDTO.mensaje.
 */
export function mensajeDeError(error: unknown): string {
  if (!(error instanceof HttpErrorResponse)) {
    return 'Ocurrió un error inesperado. Inténtalo de nuevo.';
  }
  switch (error.status) {
    case 0:
      return 'No se pudo conectar con el servidor. Revisa tu conexión e inténtalo de nuevo.';
    case 401:
      return 'Tu sesión venció. Vuelve a iniciar sesión.';
    case 403:
      return 'No tienes permiso para realizar esta acción con tu rol actual.';
  }
  const cuerpo = error.error as Partial<ErrorResponse> | null;
  return cuerpo?.mensaje ?? 'No se pudo completar la operación. Inténtalo de nuevo.';
}
