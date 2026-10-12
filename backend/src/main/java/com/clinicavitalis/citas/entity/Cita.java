package com.clinicavitalis.citas.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Cita médica (tabla {@code cita}).
 *
 * <p>Dos protecciones evitan que un turno se reserve dos veces:
 * <ul>
 *   <li>En la BD, el índice único {@code uk_cita_slot} sobre la columna calculada
 *       {@code slot_activo} (no se mapea aquí porque la calcula MySQL).</li>
 *   <li>En JPA, el bloqueo optimista con {@link Version}.</li>
 * </ul>
 */
@Entity
@Table(name = "cita")
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cita")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_paciente", nullable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_medico", nullable = false)
    private Medico medico;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "hora", nullable = false)
    private LocalTime hora;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoCita estado = EstadoCita.PENDIENTE;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal", nullable = false)
    private CanalCita canal;

    @Column(name = "motivo", length = 250)
    private String motivo;

    @Column(name = "motivo_cancelacion", length = 250)
    private String motivoCancelacion;

    /** Fin de la reserva temporal: si no se confirma antes, la cita expira (5 min). */
    @Column(name = "expira_en")
    private LocalDateTime expiraEn;

    /** Usuario que registró la cita (paciente en el portal o recepcionista). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registrado_por")
    private Usuario registradoPor;

    @Column(name = "fecha_registro", insertable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_actualizacion", insertable = false, updatable = false)
    private LocalDateTime fechaActualizacion;

    @Version
    @Column(name = "version", nullable = false)
    private int version;

    protected Cita() {
        // requerido por JPA
    }

    public Cita(Paciente paciente, Medico medico, LocalDate fecha, LocalTime hora,
                CanalCita canal, Usuario registradoPor) {
        this.paciente = paciente;
        this.medico = medico;
        this.fecha = fecha;
        this.hora = hora;
        this.canal = canal;
        this.registradoPor = registradoPor;
    }

    /** Fecha y hora juntas, útil para comparar con "ahora". */
    public LocalDateTime getFechaHora() {
        return LocalDateTime.of(fecha, hora);
    }

    /** Indica si la reserva temporal ya venció sin confirmarse. */
    public boolean reservaVencida(LocalDateTime ahora) {
        return estado == EstadoCita.PENDIENTE && expiraEn != null && !expiraEn.isAfter(ahora);
    }

    /** Pasa la cita de PENDIENTE a CONFIRMADA (RF009). */
    public void confirmar() {
        exigirEstado(EstadoCita.PENDIENTE, "confirmar");
        estado = EstadoCita.CONFIRMADA;
        expiraEn = null;
    }

    /** Cancela la cita y libera el turno (RF012). */
    public void cancelar(String motivo) {
        if (!estado.ocupaTurno() || estado == EstadoCita.ATENDIDA || estado == EstadoCita.NO_ASISTIO) {
            throw new IllegalStateException("No se puede cancelar una cita en estado " + estado);
        }
        estado = EstadoCita.CANCELADA;
        motivoCancelacion = motivo;
    }

    /** Marca como expirada una reserva temporal que no se confirmó. */
    public void expirar() {
        exigirEstado(EstadoCita.PENDIENTE, "expirar");
        estado = EstadoCita.EXPIRADA;
    }

    /** El médico registra que atendió al paciente (RF013). */
    public void marcarAtendida() {
        exigirEstado(EstadoCita.CONFIRMADA, "marcar como atendida");
        estado = EstadoCita.ATENDIDA;
    }

    /** Recepción registra que el paciente no se presentó. */
    public void marcarNoAsistio() {
        exigirEstado(EstadoCita.CONFIRMADA, "marcar como no asistida");
        estado = EstadoCita.NO_ASISTIO;
    }

    private void exigirEstado(EstadoCita esperado, String accion) {
        if (estado != esperado) {
            throw new IllegalStateException(
                    "No se puede " + accion + " una cita en estado " + estado);
        }
    }

    public Long getId() {
        return id;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public Medico getMedico() {
        return medico;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public EstadoCita getEstado() {
        return estado;
    }

    public CanalCita getCanal() {
        return canal;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getMotivoCancelacion() {
        return motivoCancelacion;
    }

    public LocalDateTime getExpiraEn() {
        return expiraEn;
    }

    public void setExpiraEn(LocalDateTime expiraEn) {
        this.expiraEn = expiraEn;
    }

    public Usuario getRegistradoPor() {
        return registradoPor;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public int getVersion() {
        return version;
    }
}
