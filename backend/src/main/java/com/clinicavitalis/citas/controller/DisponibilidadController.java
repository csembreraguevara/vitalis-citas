package com.clinicavitalis.citas.controller;

import com.clinicavitalis.citas.dto.DisponibilidadDTO;
import com.clinicavitalis.citas.service.DisponibilidadService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/** Turnos libres y ocupados de un médico (RF008). */
@RestController
@RequestMapping("/api/disponibilidad")
public class DisponibilidadController {

    private final DisponibilidadService disponibilidadService;

    public DisponibilidadController(DisponibilidadService disponibilidadService) {
        this.disponibilidadService = disponibilidadService;
    }

    /** GET /api/disponibilidad?medicoId=1&fecha=2026-11-20 */
    @GetMapping
    @PreAuthorize("hasAnyRole('PACIENTE', 'RECEPCIONISTA')")
    public DisponibilidadDTO consultar(@RequestParam Long medicoId,
                                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return disponibilidadService.consultar(medicoId, fecha);
    }
}
