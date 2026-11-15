package com.clinicavitalis.citas.dto;

import com.clinicavitalis.citas.entity.EstadoCita;

/** Fila intermedia de la consulta JPQL: citas por médico y estado. */
public record ConteoMedicoEstadoDTO(Long idMedico, String nombres, String apellidos, String especialidad,
                                    EstadoCita estado, Long total) {
}
