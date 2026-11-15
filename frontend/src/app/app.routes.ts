import { Routes } from '@angular/router';

import { authGuard, invitadoGuard } from './core/guards/auth.guard';
import { rolGuard } from './core/guards/rol.guard';
import { Login } from './features/login/login';
import { Panel } from './features/panel/panel';
import { SinPermiso } from './features/sin-permiso/sin-permiso';
import { Shell } from './layout/shell/shell';

export const routes: Routes = [
  {
    path: 'login',
    component: Login,
    canActivate: [invitadoGuard],
    title: 'Iniciar sesión · Clínica Vitalis',
  },
  {
    // Todas las páginas privadas comparten el layout (barra + menú).
    path: '',
    component: Shell,
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'panel' },
      { path: 'panel', component: Panel, title: 'Inicio · Clínica Vitalis' },
      {
        path: 'reservar',
        loadComponent: () => import('./features/reservar/reservar').then((m) => m.Reservar),
        canActivate: [rolGuard],
        data: { roles: ['PACIENTE', 'RECEPCIONISTA'] },
        title: 'Reservar cita · Clínica Vitalis',
      },
      {
        path: 'mis-citas',
        loadComponent: () => import('./features/mis-citas/mis-citas').then((m) => m.MisCitas),
        canActivate: [rolGuard],
        data: { roles: ['PACIENTE'] },
        title: 'Mis citas · Clínica Vitalis',
      },
      {
        path: 'reportes',
        loadComponent: () => import('./features/reportes/reportes').then((m) => m.Reportes),
        canActivate: [rolGuard],
        data: { roles: ['ADMIN'] },
        title: 'Reportes · Clínica Vitalis',
      },
      { path: 'sin-permiso', component: SinPermiso, title: 'Sin permiso · Clínica Vitalis' },
    ],
  },
  { path: '**', redirectTo: '' },
];
