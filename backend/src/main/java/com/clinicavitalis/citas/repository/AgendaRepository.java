package com.clinicavitalis.citas.repository;

import com.clinicavitalis.citas.entity.Cita;
import com.clinicavitalis.citas.entity.Medico;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Consultas JPQL de la agenda diaria (RF013). */
public interface AgendaRepository extends Repository<Cita, Long> {

    /** Citas del día (sin reservas expiradas); si idMedico es null, de todos los médicos. */
    @Query("""
            select c from Cita c
            join fetch c.medico m join fetch m.especialidad
            join fetch c.paciente
            where c.fecha = :fecha
              and (:idMedico is null or m.id = :idMedico)
              and c.estado <> com.clinicavitalis.citas.entity.EstadoCita.EXPIRADA
            order by c.hora, m.apellidos
            """)
    List<Cita> citasDelDia(@Param("fecha") LocalDate fecha, @Param("idMedico") Long idMedico);

    /** Médico asociado a una cuenta de usuario (rol MEDICO). */
    @Query("select m from Medico m join fetch m.especialidad where m.usuario.username = :username")
    Optional<Medico> medicoDeUsuario(@Param("username") String username);

    @Query("""
            select c from Cita c
            join fetch c.medico m join fetch m.especialidad
            join fetch c.paciente
            where c.id = :id
            """)
    Optional<Cita> buscarCita(@Param("id") Long id);
}
