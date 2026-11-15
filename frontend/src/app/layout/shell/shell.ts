import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { NOMBRE_ROL, opcionesPara } from '../../core/navegacion';
import { AuthService } from '../../core/services/auth.service';

/** Marco de las páginas privadas: barra superior, menú lateral según el rol y contenido. */
@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './shell.html',
  styleUrl: './shell.scss',
})
export class Shell {
  protected readonly auth = inject(AuthService);

  /** En celular el menú se abre y cierra con el botón ☰. */
  protected readonly menuAbierto = signal(false);

  protected readonly opciones = computed(() => opcionesPara(this.auth.usuario()?.rol));

  protected readonly nombreRol = computed(() => {
    const rol = this.auth.usuario()?.rol;
    return rol ? NOMBRE_ROL[rol] : '';
  });
}
