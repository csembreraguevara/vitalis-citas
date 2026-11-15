package com.clinicavitalis.citas.dto;

import java.time.LocalDate;
import java.util.List;

/** Turnos libres y ocupados de un médico en una fecha (RF008). */
public record DisponibilidadDTO(
        Long idMedico,
        String medico,
        String especialidad,
        String consultorio,
        LocalDate fecha,
        int duracionMin,
        List<TurnoDTO> turnos) {
}
