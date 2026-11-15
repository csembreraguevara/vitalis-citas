import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { UsuarioActual } from '../../core/models/auth.models';
import { NOMBRE_ROL, opcionesPara } from '../../core/navegacion';
import { AuthService } from '../../core/services/auth.service';

/** Página de inicio después de iniciar sesión. La barra y el menú los pone el Shell. */
@Component({
  selector: 'app-panel',
  imports: [RouterLink],
  templateUrl: './panel.html',
  styleUrl: './panel.scss',
})
export class Panel implements OnInit {
  protected readonly auth = inject(AuthService);

  protected readonly verificado = signal<UsuarioActual | null>(null);
  protected readonly errorVerificacion = signal(false);

  protected readonly nombreRol = computed(() => {
    const rol = this.auth.usuario()?.rol;
    return rol ? NOMBRE_ROL[rol] : '';
  });

  /** Mismas opciones que el menú lateral, como tarjetas de acceso rápido. */
  protected readonly opciones = computed(() => opcionesPara(this.auth.usuario()?.rol));

  ngOnInit(): void {
    // Llama a una ruta protegida para comprobar que el backend acepta el token.
    this.auth.usuarioActual().subscribe({
      next: (usuario) => this.verificado.set(usuario),
      error: () => this.errorVerificacion.set(true),
    });
  }
}
