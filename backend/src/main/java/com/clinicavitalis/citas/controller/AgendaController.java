package com.clinicavitalis.citas.controller;

import com.clinicavitalis.citas.dto.CitaDTO;
import com.clinicavitalis.citas.entity.Rol;
import com.clinicavitalis.citas.service.AgendaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/** Agenda del día y registro de asistencia (RF013). */
@RestController
@RequestMapping("/api/agenda")
@PreAuthorize("hasAnyRole('RECEPCIONISTA', 'MEDICO', 'ADMIN')")
public class AgendaController {

    private final AgendaService agendaService;

    public AgendaController(AgendaService agendaService) {
        this.agendaService = agendaService;
    }

    /** GET /api/agenda?fecha=2026-11-16&medicoId=1 (medicoId opcional; el médico ve solo la suya) */
    @GetMapping
    public List<CitaDTO> citasDelDia(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                                     @RequestParam(required = false) Long medicoId,
                                     Authentication autenticacion) {
        return agendaService.citasDelDia(fecha, medicoId, autenticacion.getName(), rolDe(autenticacion));
    }

    @PatchMapping("/citas/{id}/atendida")
    @PreAuthorize("hasAnyRole('RECEPCIONISTA', 'MEDICO')")
    public CitaDTO marcarAtendida(@PathVariable Long id, Authentication autenticacion) {
        return agendaService.marcarAtendida(id, autenticacion.getName(), rolDe(autenticacion));
    }

    @PatchMapping("/citas/{id}/no-asistio")
    @PreAuthorize("hasAnyRole('RECEPCIONISTA', 'MEDICO')")
    public CitaDTO marcarNoAsistio(@PathVariable Long id, Authentication autenticacion) {
        return agendaService.marcarNoAsistio(id, autenticacion.getName(), rolDe(autenticacion));
    }

    private static Rol rolDe(Authentication autenticacion) {
        return autenticacion.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(autoridad -> autoridad.startsWith("ROLE_"))
                .map(autoridad -> Rol.valueOf(autoridad.substring("ROLE_".length())))
                .findFirst()
                .orElseThrow();
    }
}
