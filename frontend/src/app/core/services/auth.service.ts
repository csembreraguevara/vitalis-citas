import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';

import { environment } from '../../../environments/environment';
import { LoginRequest, Sesion, TokenResponse, UsuarioActual } from '../models/auth.models';

const CLAVE_SESION = 'vitalis.sesion';

/**
 * Maneja el inicio y cierre de sesión (RF002).
 * Guarda el token JWT en sessionStorage: se borra al cerrar la pestaña.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly sesion = signal<Sesion | null>(this.leerSesion());

  /** Usuario conectado, o null si no hay sesión. */
  readonly usuario = computed(() => this.sesion());

  /** true mientras haya un token vigente. */
  readonly autenticado = computed(() => {
    const actual = this.sesion();
    return actual !== null && actual.expiraEn > Date.now();
  });

  login(credenciales: LoginRequest): Observable<TokenResponse> {
    return this.http.post<TokenResponse>(`${environment.apiUrl}/auth/login`, credenciales).pipe(
      tap((respuesta) =>
        this.guardarSesion({
          token: respuesta.token,
          username: respuesta.username,
          rol: respuesta.rol,
          expiraEn: Date.now() + respuesta.expiraEnSegundos * 1000,
        }),
      ),
    );
  }

  /** Pide al backend los datos del usuario del token (verifica que el token sea válido). */
  usuarioActual(): Observable<UsuarioActual> {
    return this.http.get<UsuarioActual>(`${environment.apiUrl}/auth/me`);
  }

  logout(): void {
    this.guardarSesion(null);
    this.router.navigate(['/login']);
  }

  /** Token para el interceptor; null si no hay sesión o ya venció. */
  token(): string | null {
    return this.autenticado() ? this.sesion()!.token : null;
  }

  private guardarSesion(sesion: Sesion | null): void {
    this.sesion.set(sesion);
    try {
      if (sesion) {
        sessionStorage.setItem(CLAVE_SESION, JSON.stringify(sesion));
      } else {
        sessionStorage.removeItem(CLAVE_SESION);
      }
    } catch {
      // Si el navegador bloquea el almacenamiento, la sesión vive solo en memoria.
    }
  }

  private leerSesion(): Sesion | null {
    try {
      const guardada = sessionStorage.getItem(CLAVE_SESION);
      return guardada ? (JSON.parse(guardada) as Sesion) : null;
    } catch {
      return null;
    }
  }
}
