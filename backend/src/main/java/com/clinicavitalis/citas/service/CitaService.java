package com.clinicavitalis.citas.service;

import com.clinicavitalis.citas.dto.CitaDTO;
import com.clinicavitalis.citas.dto.CitaRequestDTO;
import com.clinicavitalis.citas.entity.Rol;

import java.util.List;

/** Reserva, confirmación, cancelación y consulta de citas (RF009–RF012). */
public interface CitaService {

    CitaDTO reservar(CitaRequestDTO solicitud, String username, Rol rol);

    CitaDTO confirmar(Long idCita, String username);

    CitaDTO cancelar(Long idCita, String motivo, String username, Rol rol);

    List<CitaDTO> misCitas(String username);
}
