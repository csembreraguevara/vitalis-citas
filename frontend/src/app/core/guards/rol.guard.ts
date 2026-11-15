import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { Rol } from '../models/auth.models';
import { AuthService } from '../services/auth.service';

/**
 * Deja entrar solo a los roles indicados en la ruta: `data: { roles: ['ADMIN'] }`.
 * Si el rol no corresponde, lleva a /sin-permiso. Es una ayuda de navegación:
 * la seguridad real la aplica el backend con @PreAuthorize (responde 403).
 */
export const rolGuard: CanActivateFn = (route) => {
  const permitidos = (route.data['roles'] ?? []) as Rol[];
  const rol = inject(AuthService).usuario()?.rol;
  return rol && permitidos.includes(rol) ? true : inject(Router).createUrlTree(['/sin-permiso']);
};
