package com.clinicavitalis.citas.controller;

import com.clinicavitalis.citas.dto.PacienteRequestDTO;
import com.clinicavitalis.citas.dto.PacienteResumenDTO;
import com.clinicavitalis.citas.service.PacienteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Búsqueda y registro de pacientes por recepción (RF004, RF010). */
@RestController
@RequestMapping("/api/pacientes")
@PreAuthorize("hasAnyRole('RECEPCIONISTA', 'ADMIN')")
public class PacienteController {

    private final PacienteService pacienteService;

    public PacienteController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    /** GET /api/pacientes/dni/45781203 */
    @GetMapping("/dni/{dni}")
    public PacienteResumenDTO buscarPorDni(
            @PathVariable @Pattern(regexp = "\\d{8}", message = "el DNI tiene 8 dígitos") String dni) {
        return pacienteService.buscarPorDni(dni);
    }

    /** POST /api/pacientes: registra un paciente nuevo (sin cuenta en el portal). */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PacienteResumenDTO registrar(@Valid @RequestBody PacienteRequestDTO solicitud,
                                        Authentication autenticacion) {
        return pacienteService.registrar(solicitud, autenticacion.getName());
    }
}
