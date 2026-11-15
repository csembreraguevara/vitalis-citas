package com.clinicavitalis.citas.controller;

import com.clinicavitalis.citas.dto.EspecialidadDTO;
import com.clinicavitalis.citas.dto.MedicoDTO;
import com.clinicavitalis.citas.service.CatalogoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Catálogo público: lo ve cualquier visitante sin iniciar sesión (RF005, RF006). */
@RestController
@RequestMapping("/api")
public class CatalogoController {

    private final CatalogoService catalogoService;

    public CatalogoController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @GetMapping("/especialidades")
    public List<EspecialidadDTO> especialidades() {
        return catalogoService.listarEspecialidades();
    }

    /** GET /api/medicos?especialidadId=1 (el filtro es opcional). */
    @GetMapping("/medicos")
    public List<MedicoDTO> medicos(@RequestParam(required = false) Long especialidadId) {
        return catalogoService.listarMedicos(especialidadId);
    }
}
