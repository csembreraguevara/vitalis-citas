package com.clinicavitalis.citas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Datos de entrada del inicio de sesión (RF002). */
public record LoginRequestDTO(
        @NotBlank(message = "El usuario es obligatorio")
        @Size(max = 100, message = "El usuario no debe superar los 100 caracteres")
        String username,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(max = 72, message = "La contraseña no debe superar los 72 caracteres")
        String password) {
}
