import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { PacienteRequest, PacienteResumen } from '../models/citas.models';

/** Registro de pacientes por recepción (RF004). */
@Injectable({ providedIn: 'root' })
export class PacienteService {
  private readonly http = inject(HttpClient);

  registrar(solicitud: PacienteRequest): Observable<PacienteResumen> {
    return this.http.post<PacienteResumen>(`${environment.apiUrl}/pacientes`, solicitud);
  }
}
