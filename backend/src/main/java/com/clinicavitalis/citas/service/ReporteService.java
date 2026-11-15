package com.clinicavitalis.citas.service;

import com.clinicavitalis.citas.dto.OcupacionDTO;

import java.time.LocalDate;
import java.util.List;

/** Reportes de gestión (RF016). */
public interface ReporteService {

    List<OcupacionDTO> ocupacion(LocalDate desde, LocalDate hasta);
}
