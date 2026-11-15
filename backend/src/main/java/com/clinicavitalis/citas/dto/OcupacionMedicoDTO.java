package com.clinicavitalis.citas.dto;

/** Detalle del reporte R01 por médico: mismos conteos que {@link OcupacionDTO}. */
public record OcupacionMedicoDTO(
        Long idMedico,
        String medico,
        String especialidad,
        long pendientes,
        long confirmadas,
        long atendidas,
        long noAsistio,
        long canceladas,
        long total) {
}
