import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

/** Se muestra cuando el rol no puede entrar a una página (equivale al 403 del backend). */
@Component({
  selector: 'app-sin-permiso',
  imports: [RouterLink],
  template: `
    <section class="text-center py-5">
      <p class="display-6 fw-bold text-secondary mb-2">403</p>
      <h1 class="h4 fw-bold">No tienes permiso para ver esta página</h1>
      <p class="text-secondary mb-4">
        Esta opción requiere otro rol. Si crees que es un error, contacta al administrador.
      </p>
      <a routerLink="/panel" class="btn btn-vitalis">Volver al inicio</a>
    </section>
  `,
})
export class SinPermiso {}
