package com.clinicavitalis.citas.dto;

import com.clinicavitalis.citas.entity.Cita;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** Cita tal como la ve el frontend. */
public record CitaDTO(
        Long id,
        LocalDate fecha,
        LocalTime hora,
        String estado,
        String canal,
        Long idMedico,
        String medico,
        String especialidad,
        String consultorio,
        String paciente,
        String motivo,
        LocalDateTime expiraEn) {

    /** Requiere médico, especialidad y paciente cargados (JOIN FETCH). */
    public static CitaDTO de(Cita cita) {
        return new CitaDTO(cita.getId(), cita.getFecha(), cita.getHora(), cita.getEstado().name(),
                cita.getCanal().name(), cita.getMedico().getId(), cita.getMedico().getNombreCompleto(),
                cita.getMedico().getEspecialidad().getNombre(), cita.getMedico().getConsultorio(),
                cita.getPaciente().getNombreCompleto(), cita.getMotivo(), cita.getExpiraEn());
    }
}
