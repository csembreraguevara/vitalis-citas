package com.clinicavitalis.citas.service;

import com.clinicavitalis.citas.dto.CitaDTO;
import com.clinicavitalis.citas.dto.OcupacionMedicoDTO;

import java.time.LocalDate;
import java.util.List;

/** Detalle del reporte de ocupación: por médico y sus citas (RF016). */
public interface ReporteDetalleService {

    List<OcupacionMedicoDTO> ocupacionPorMedico(LocalDate desde, LocalDate hasta);

    List<CitaDTO> citasDelMedico(Long idMedico, LocalDate desde, LocalDate hasta);
}
