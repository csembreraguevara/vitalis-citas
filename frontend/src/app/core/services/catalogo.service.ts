import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Disponibilidad, Especialidad, Medico } from '../models/citas.models';

/** Consultas de especialidades, médicos y turnos (RF005, RF006, RF008). */
@Injectable({ providedIn: 'root' })
export class CatalogoService {
  private readonly http = inject(HttpClient);
  private readonly api = environment.apiUrl;

  especialidades(): Observable<Especialidad[]> {
    return this.http.get<Especialidad[]>(`${this.api}/especialidades`);
  }

  medicos(idEspecialidad: number): Observable<Medico[]> {
    const params = new HttpParams().set('especialidadId', idEspecialidad);
    return this.http.get<Medico[]>(`${this.api}/medicos`, { params });
  }

  /** Turnos del médico en una fecha "yyyy-MM-dd". Requiere sesión. */
  disponibilidad(idMedico: number, fecha: string): Observable<Disponibilidad> {
    const params = new HttpParams().set('medicoId', idMedico).set('fecha', fecha);
    return this.http.get<Disponibilidad>(`${this.api}/disponibilidad`, { params });
  }
}
