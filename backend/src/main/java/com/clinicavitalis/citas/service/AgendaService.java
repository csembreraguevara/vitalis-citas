package com.clinicavitalis.citas.service;

import com.clinicavitalis.citas.dto.CitaDTO;
import com.clinicavitalis.citas.entity.Rol;

import java.time.LocalDate;
import java.util.List;

/** Agenda del día y registro de asistencia (RF013). */
public interface AgendaService {

    /** El médico solo ve su agenda; recepción y administración pueden filtrar por médico. */
    List<CitaDTO> citasDelDia(LocalDate fecha, Long idMedico, String username, Rol rol);

    CitaDTO marcarAtendida(Long idCita, String username, Rol rol);

    CitaDTO marcarNoAsistio(Long idCita, String username, Rol rol);
}
