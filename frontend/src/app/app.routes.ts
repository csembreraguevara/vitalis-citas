import { Routes } from '@angular/router';

import { authGuard, invitadoGuard } from './core/guards/auth.guard';
import { Login } from './features/login/login';
import { Panel } from './features/panel/panel';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'login' },
  {
    path: 'login',
    component: Login,
    canActivate: [invitadoGuard],
    title: 'Iniciar sesión · Clínica Vitalis',
  },
  { path: 'panel', component: Panel, canActivate: [authGuard], title: 'Panel · Clínica Vitalis' },
  { path: '**', redirectTo: 'login' },
];
