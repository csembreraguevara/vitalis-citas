package com.clinicavitalis.citas.dto;

import com.clinicavitalis.citas.entity.EstadoCita;

/** Fila intermedia de la consulta JPQL del reporte: citas por especialidad y estado. */
public record ConteoEstadoDTO(String especialidad, EstadoCita estado, Long total) {
}
