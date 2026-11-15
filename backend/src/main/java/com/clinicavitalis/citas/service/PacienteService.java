package com.clinicavitalis.citas.service;

import com.clinicavitalis.citas.dto.PacienteRequestDTO;
import com.clinicavitalis.citas.dto.PacienteResumenDTO;

/** Registro y búsqueda de pacientes (RF004). */
public interface PacienteService {

    PacienteResumenDTO buscarPorDni(String dni);

    PacienteResumenDTO registrar(PacienteRequestDTO solicitud, String registradoPor);
}
