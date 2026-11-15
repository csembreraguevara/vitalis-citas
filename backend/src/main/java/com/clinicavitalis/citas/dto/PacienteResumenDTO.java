package com.clinicavitalis.citas.dto;

import com.clinicavitalis.citas.entity.Paciente;

/** Datos mínimos del paciente para que recepción confirme a quién le registra la cita. */
public record PacienteResumenDTO(Long id, String dni, String nombreCompleto) {

    public static PacienteResumenDTO de(Paciente paciente) {
        return new PacienteResumenDTO(paciente.getId(), paciente.getDni(), paciente.getNombreCompleto());
    }
}
