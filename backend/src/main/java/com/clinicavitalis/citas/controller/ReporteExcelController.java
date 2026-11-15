package com.clinicavitalis.citas.controller;

import com.clinicavitalis.citas.service.ReporteExcelService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/** Descarga del reporte de ocupación en Excel: solo el administrador (RF017). */
@RestController
@RequestMapping("/api/reportes")
public class ReporteExcelController {

    private static final MediaType XLSX =
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final ReporteExcelService reporteExcelService;

    public ReporteExcelController(ReporteExcelService reporteExcelService) {
        this.reporteExcelService = reporteExcelService;
    }

    /** GET /api/reportes/ocupacion/excel?desde=2026-11-01&hasta=2026-11-30 */
    @GetMapping("/ocupacion/excel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> ocupacionExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        String nombre = "ocupacion_" + desde + "_" + hasta + ".xlsx";
        return ResponseEntity.ok()
                .contentType(XLSX)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(nombre).build().toString())
                .body(reporteExcelService.ocupacion(desde, hasta));
    }
}
