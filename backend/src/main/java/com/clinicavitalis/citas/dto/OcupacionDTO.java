package com.clinicavitalis.citas.dto;

/** Fila del reporte de ocupación R01 (RF016): citas por especialidad en un rango de fechas. */
public record OcupacionDTO(
        String especialidad,
        long pendientes,
        long confirmadas,
        long atendidas,
        long noAsistio,
        long canceladas,
        long total) {
}
