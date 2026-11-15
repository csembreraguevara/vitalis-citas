package com.clinicavitalis.citas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Motivo obligatorio para cancelar una cita (RF012). */
public record CancelarCitaRequestDTO(
        @NotBlank(message = "indica el motivo")
        @Size(max = 250, message = "máximo 250 caracteres") String motivo) {
}
