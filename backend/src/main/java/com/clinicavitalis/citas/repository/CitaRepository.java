package com.clinicavitalis.citas.repository;

import com.clinicavitalis.citas.dto.ConteoEstadoDTO;
import com.clinicavitalis.citas.entity.Cita;
import com.clinicavitalis.citas.entity.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** DAO de la tabla {@code cita} con las consultas JPQL del módulo de agendamiento. */
public interface CitaRepository extends JpaRepository<Cita, Long> {

    /** Horas que ya están tomadas por citas en alguno de los estados indicados (RN01). */
    @Query("""
            select c.hora from Cita c
            where c.medico.id = :idMedico and c.fecha = :fecha and c.estado in :estados
            """)
    List<LocalTime> horasOcupadas(@Param("idMedico") Long idMedico,
                                  @Param("fecha") LocalDate fecha,
                                  @Param("estados") Collection<EstadoCita> estados);

    boolean existsByMedicoIdAndFechaAndHoraAndEstadoIn(Long idMedico, LocalDate fecha, LocalTime hora,
                                                       Collection<EstadoCita> estados);

    /** Citas activas del paciente que todavía no ocurren (RN05). */
    @Query("""
            select count(c) from Cita c
            where c.paciente.id = :idPaciente and c.estado in :estados
              and (c.fecha > :hoy or (c.fecha = :hoy and c.hora > :hora))
            """)
    long contarFuturas(@Param("idPaciente") Long idPaciente,
                       @Param("estados") Collection<EstadoCita> estados,
                       @Param("hoy") LocalDate hoy,
                       @Param("hora") LocalTime hora);

    @Query("""
            select c from Cita c
            join fetch c.medico m join fetch m.especialidad
            join fetch c.paciente
            where c.paciente.id = :idPaciente
            order by c.fecha desc, c.hora desc
            """)
    List<Cita> buscarPorPaciente(@Param("idPaciente") Long idPaciente);

    /** Cita con todo lo necesario para responder y validar al dueño. */
    @Query("""
            select c from Cita c
            join fetch c.medico m join fetch m.especialidad
            join fetch c.paciente p left join fetch p.usuario
            where c.id = :id
            """)
    Optional<Cita> buscarDetalle(@Param("id") Long id);

    /** Cambia de estado las reservas temporales vencidas (RN02). */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update Cita c set c.estado = :nuevo, c.version = c.version + 1
            where c.estado = :actual and c.expiraEn <= :ahora
            """)
    int cambiarEstadoVencidas(@Param("actual") EstadoCita actual,
                              @Param("nuevo") EstadoCita nuevo,
                              @Param("ahora") LocalDateTime ahora);

    /** Libera los turnos de las reservas PENDIENTE que no se confirmaron a tiempo (RN02). */
    default int expirarReservasVencidas(LocalDateTime ahora) {
        return cambiarEstadoVencidas(EstadoCita.PENDIENTE, EstadoCita.EXPIRADA, ahora);
    }

    /** Conteo de citas por especialidad y estado para el reporte R01 (RF016). */
    @Query("""
            select new com.clinicavitalis.citas.dto.ConteoEstadoDTO(e.nombre, c.estado, count(c))
            from Cita c join c.medico m join m.especialidad e
            where c.fecha between :desde and :hasta
            group by e.nombre, c.estado
            order by e.nombre
            """)
    List<ConteoEstadoDTO> contarPorEspecialidadYEstado(@Param("desde") LocalDate desde,
                                                       @Param("hasta") LocalDate hasta);
}
