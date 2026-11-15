import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Cita, Medico } from '../models/citas.models';

/** Agenda del día y registro de asistencia (RF013). */
@Injectable({ providedIn: 'root' })
export class AgendaService {
  private readonly http = inject(HttpClient);
  private readonly api = `${environment.apiUrl}/agenda`;

  /** El backend ignora medicoId si quien consulta es médico (ve solo su agenda). */
  citasDelDia(fecha: string, idMedico: number | null): Observable<Cita[]> {
    let params = new HttpParams().set('fecha', fecha);
    if (idMedico !== null) {
      params = params.set('medicoId', idMedico);
    }
    return this.http.get<Cita[]>(this.api, { params });
  }

  marcarAtendida(idCita: number): Observable<Cita> {
    return this.http.patch<Cita>(`${this.api}/citas/${idCita}/atendida`, {});
  }

  marcarNoAsistio(idCita: number): Observable<Cita> {
    return this.http.patch<Cita>(`${this.api}/citas/${idCita}/no-asistio`, {});
  }

  /** Todos los médicos activos, para el filtro de recepción. */
  medicos(): Observable<Medico[]> {
    return this.http.get<Medico[]>(`${environment.apiUrl}/medicos`);
  }
}
