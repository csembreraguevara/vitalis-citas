package com.clinicavitalis.citas.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Datos para reservar una cita (RF009, RF010).
 * {@code idPaciente} solo lo envía recepción; el paciente se toma del token.
 */
public record CitaRequestDTO(
        @NotNull(message = "elige un médico") Long idMedico,
        @NotNull(message = "elige una fecha") LocalDate fecha,
        @NotNull(message = "elige una hora") LocalTime hora,
        @Size(max = 250, message = "máximo 250 caracteres") String motivo,
        Long idPaciente) {
}
