package com.clinicavitalis.citas.controller;

import com.clinicavitalis.citas.dto.CitaDTO;
import com.clinicavitalis.citas.dto.OcupacionMedicoDTO;
import com.clinicavitalis.citas.service.ReporteDetalleService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/** Detalle del reporte de ocupación: solo el administrador (RF016). */
@RestController
@RequestMapping("/api/reportes")
@PreAuthorize("hasRole('ADMIN')")
public class ReporteDetalleController {

    private final ReporteDetalleService reporteDetalleService;

    public ReporteDetalleController(ReporteDetalleService reporteDetalleService) {
        this.reporteDetalleService = reporteDetalleService;
    }

    /** GET /api/reportes/ocupacion/medicos?desde=2026-11-01&hasta=2026-11-30 */
    @GetMapping("/ocupacion/medicos")
    public List<OcupacionMedicoDTO> porMedico(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return reporteDetalleService.ocupacionPorMedico(desde, hasta);
    }

    /** GET /api/reportes/medicos/1/citas?desde=2026-11-01&hasta=2026-11-30 */
    @GetMapping("/medicos/{idMedico}/citas")
    public List<CitaDTO> citasDelMedico(
            @PathVariable Long idMedico,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return reporteDetalleService.citasDelMedico(idMedico, desde, hasta);
    }
}
