/** Roles del sistema (mismo enum que el backend). */
export type Rol = 'PACIENTE' | 'RECEPCIONISTA' | 'MEDICO' | 'ADMIN';

/** Cuerpo de POST /api/auth/login (LoginRequestDTO). */
export interface LoginRequest {
  username: string;
  password: string;
}

/** Respuesta de POST /api/auth/login (TokenResponseDTO). */
export interface TokenResponse {
  token: string;
  tipo: string;
  expiraEnSegundos: number;
  username: string;
  rol: Rol;
}

/** Respuesta de GET /api/auth/me (UsuarioActualDTO). */
export interface UsuarioActual {
  username: string;
  rol: Rol;
}

/** Formato de error que devuelve el GlobalExceptionHandler del backend. */
export interface ErrorResponse {
  status: number;
  error: string;
  mensaje: string;
  detalles: string[];
  fecha: string;
}

/** Sesión guardada en el navegador. */
export interface Sesion {
  token: string;
  username: string;
  rol: Rol;
  expiraEn: number;
}
