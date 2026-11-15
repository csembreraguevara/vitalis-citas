package com.clinicavitalis.citas.controller;

import com.clinicavitalis.citas.dto.PacienteResumenDTO;
import com.clinicavitalis.citas.exception.RecursoNoEncontradoException;
import com.clinicavitalis.citas.repository.PacienteRepository;
import jakarta.validation.constraints.Pattern;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Búsqueda de pacientes por DNI para recepción (RF004, RF010). */
@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {

    private final PacienteRepository pacienteRepository;

    public PacienteController(PacienteRepository pacienteRepository) {
        this.pacienteRepository = pacienteRepository;
    }

    /** GET /api/pacientes/dni/45781203 */
    @GetMapping("/dni/{dni}")
    @PreAuthorize("hasAnyRole('RECEPCIONISTA', 'ADMIN')")
    @Transactional(readOnly = true)
    public PacienteResumenDTO buscarPorDni(
            @PathVariable @Pattern(regexp = "\\d{8}", message = "el DNI tiene 8 dígitos") String dni) {
        return pacienteRepository.findByDni(dni)
                .map(PacienteResumenDTO::de)
                .orElseThrow(() -> new RecursoNoEncontradoException("No hay un paciente registrado con ese DNI"));
    }
}
