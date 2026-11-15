package com.clinicavitalis.citas.controller;

import com.clinicavitalis.citas.dto.CancelarCitaRequestDTO;
import com.clinicavitalis.citas.dto.CitaDTO;
import com.clinicavitalis.citas.dto.CitaRequestDTO;
import com.clinicavitalis.citas.entity.Rol;
import com.clinicavitalis.citas.service.CitaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Agendamiento de citas (RF009–RF012). */
@RestController
@RequestMapping("/api/citas")
public class CitaController {

    private final CitaService citaService;

    public CitaController(CitaService citaService) {
        this.citaService = citaService;
    }

    /** Reserva: el paciente la deja PENDIENTE 5 minutos; recepción la registra CONFIRMADA. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('PACIENTE', 'RECEPCIONISTA')")
    public CitaDTO reservar(@Valid @RequestBody CitaRequestDTO solicitud, Authentication autenticacion) {
        return citaService.reservar(solicitud, autenticacion.getName(), rolDe(autenticacion));
    }

    @PatchMapping("/{id}/confirmar")
    @PreAuthorize("hasRole('PACIENTE')")
    public CitaDTO confirmar(@PathVariable Long id, Authentication autenticacion) {
        return citaService.confirmar(id, autenticacion.getName());
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('PACIENTE', 'RECEPCIONISTA')")
    public CitaDTO cancelar(@PathVariable Long id, @Valid @RequestBody CancelarCitaRequestDTO solicitud,
                            Authentication autenticacion) {
        return citaService.cancelar(id, solicitud.motivo(), autenticacion.getName(), rolDe(autenticacion));
    }

    @GetMapping("/mis-citas")
    @PreAuthorize("hasRole('PACIENTE')")
    public List<CitaDTO> misCitas(Authentication autenticacion) {
        return citaService.misCitas(autenticacion.getName());
    }

    /** El filtro JWT guarda el rol como autoridad "ROLE_<ROL>". */
    private static Rol rolDe(Authentication autenticacion) {
        return autenticacion.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(autoridad -> autoridad.startsWith("ROLE_"))
                .map(autoridad -> Rol.valueOf(autoridad.substring("ROLE_".length())))
                .findFirst()
                .orElseThrow();
    }
}
