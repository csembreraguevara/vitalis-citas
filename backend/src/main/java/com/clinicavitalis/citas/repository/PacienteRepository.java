package com.clinicavitalis.citas.repository;

import com.clinicavitalis.citas.entity.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** DAO de la tabla {@code paciente}. */
public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    /** Paciente dueño de una cuenta del portal. */
    Optional<Paciente> findByUsuarioUsername(String username);
      Optional<Paciente> findByDni(String dni);
}
