package com.clinicavitalis.citas.dto;

import com.clinicavitalis.citas.entity.Sexo;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Datos para registrar un paciente desde recepción (RF004). */
public record PacienteRequestDTO(
        @NotBlank(message = "ingresa el DNI")
        @Pattern(regexp = "\\d{8}", message = "el DNI tiene 8 dígitos") String dni,
        @NotBlank(message = "ingresa los nombres")
        @Size(max = 80, message = "máximo 80 caracteres") String nombres,
        @NotBlank(message = "ingresa los apellidos")
        @Size(max = 80, message = "máximo 80 caracteres") String apellidos,
        @NotNull(message = "ingresa la fecha de nacimiento")
        @Past(message = "la fecha de nacimiento debe ser pasada") LocalDate fechaNacimiento,
        @NotNull(message = "elige el sexo") Sexo sexo,
        @Pattern(regexp = "9\\d{8}", message = "el celular tiene 9 dígitos y empieza con 9") String telefono,
        @NotBlank(message = "ingresa el correo")
        @Email(message = "correo no válido")
        @Size(max = 120, message = "máximo 120 caracteres") String email,
        @Size(max = 200, message = "máximo 200 caracteres") String direccion) {
}
