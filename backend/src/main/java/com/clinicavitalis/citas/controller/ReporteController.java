package com.clinicavitalis.citas.controller;

import com.clinicavitalis.citas.dto.OcupacionDTO;
import com.clinicavitalis.citas.service.ReporteService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/** Reportes de gestión: solo el administrador (RF016). Otro rol recibe 403. */
@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    /** GET /api/reportes/ocupacion?desde=2026-11-01&hasta=2026-11-30 */
    @GetMapping("/ocupacion")
    @PreAuthorize("hasRole('ADMIN')")
    public List<OcupacionDTO> ocupacion(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return reporteService.ocupacion(desde, hasta);
    }
}
