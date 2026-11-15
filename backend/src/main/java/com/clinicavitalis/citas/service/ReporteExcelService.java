package com.clinicavitalis.citas.service;

import java.time.LocalDate;

/** Exportación del reporte de ocupación a Excel con Apache POI (RF017). */
public interface ReporteExcelService {

    /** Devuelve el contenido del archivo .xlsx. */
    byte[] ocupacion(LocalDate desde, LocalDate hasta);
}
