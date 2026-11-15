import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Cita } from '../models/citas.models';

/** Conteos de citas por estado (OcupacionDTO del backend). */
export interface Conteos {
  pendientes: number;
  confirmadas: number;
  atendidas: number;
  noAsistio: number;
  canceladas: number;
  total: number;
}

/** Fila del reporte R01 por especialidad. */
export interface FilaOcupacion extends Conteos {
  especialidad: string;
}

/** Detalle del reporte R01 por médico (OcupacionMedicoDTO). */
export interface FilaOcupacionMedico extends Conteos {
  idMedico: number;
  medico: string;
  especialidad: string;
}

/** Reportes de gestión (RF016). Solo ADMIN: otro rol recibe 403. */
@Injectable({ providedIn: 'root' })
export class ReporteService {
  private readonly http = inject(HttpClient);
  private readonly api = `${environment.apiUrl}/reportes`;

  ocupacion(desde: string, hasta: string): Observable<FilaOcupacion[]> {
    return this.http.get<FilaOcupacion[]>(`${this.api}/ocupacion`, {
      params: this.rango(desde, hasta),
    });
  }

  ocupacionPorMedico(desde: string, hasta: string): Observable<FilaOcupacionMedico[]> {
    return this.http.get<FilaOcupacionMedico[]>(`${this.api}/ocupacion/medicos`, {
      params: this.rango(desde, hasta),
    });
  }

  citasDelMedico(idMedico: number, desde: string, hasta: string): Observable<Cita[]> {
    return this.http.get<Cita[]>(`${this.api}/medicos/${idMedico}/citas`, {
      params: this.rango(desde, hasta),
    });
  }

  /** Archivo .xlsx generado con Apache POI en el backend (RF017). */
  ocupacionExcel(desde: string, hasta: string): Observable<Blob> {
    return this.http.get(`${this.api}/ocupacion/excel`, {
      params: this.rango(desde, hasta),
      responseType: 'blob',
    });
  }

  private rango(desde: string, hasta: string): HttpParams {
    return new HttpParams().set('desde', desde).set('hasta', hasta);
  }
}
