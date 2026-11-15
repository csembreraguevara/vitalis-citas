import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Cita, CitaRequest, PacienteResumen } from '../models/citas.models';

/** Operaciones sobre citas (RF009–RF012) y búsqueda de paciente para recepción. */
@Injectable({ providedIn: 'root' })
export class CitaService {
  private readonly http = inject(HttpClient);
  private readonly api = `${environment.apiUrl}/citas`;

  reservar(solicitud: CitaRequest): Observable<Cita> {
    return this.http.post<Cita>(this.api, solicitud);
  }

  confirmar(idCita: number): Observable<Cita> {
    return this.http.patch<Cita>(`${this.api}/${idCita}/confirmar`, {});
  }

  cancelar(idCita: number, motivo: string): Observable<Cita> {
    return this.http.patch<Cita>(`${this.api}/${idCita}/cancelar`, { motivo });
  }

  misCitas(): Observable<Cita[]> {
    return this.http.get<Cita[]>(`${this.api}/mis-citas`);
  }

  buscarPacientePorDni(dni: string): Observable<PacienteResumen> {
    return this.http.get<PacienteResumen>(`${environment.apiUrl}/pacientes/dni/${dni}`);
  }
}
