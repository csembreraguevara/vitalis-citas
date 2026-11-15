package com.clinicavitalis.citas.service;

import com.clinicavitalis.citas.dto.DisponibilidadDTO;
import com.clinicavitalis.citas.entity.Medico;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** Cálculo de turnos y disponibilidad de los médicos (RF008, RN06). */
public interface DisponibilidadService {

    DisponibilidadDTO consultar(Long idMedico, LocalDate fecha);

    /** Todas las horas de inicio de turno del médico en esa fecha, según su horario. */
    List<LocalTime> turnosDelDia(Medico medico, LocalDate fecha);
}
