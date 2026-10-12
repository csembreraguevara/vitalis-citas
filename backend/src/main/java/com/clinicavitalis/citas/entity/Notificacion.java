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

import java.time.LocalDateTime;

/**
 * Correo programado para un paciente sobre una cita (tabla {@code notificacion}).
 * Una tarea programada envía las PENDIENTE cuya fecha ya llegó (RF014, RF015).
 */
@Entity
@Table(name = "notificacion")
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notificacion")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cita", nullable = false)
    private Cita cita;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoNotificacion tipo;

    @Column(name = "destinatario", nullable = false, length = 120)
    private String destinatario;

    @Column(name = "asunto", nullable = false, length = 150)
    private String asunto;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoNotificacion estado = EstadoNotificacion.PENDIENTE;

    @Column(name = "fecha_programada", nullable = false)
    private LocalDateTime fechaProgramada;

    @Column(name = "fecha_envio")
    private LocalDateTime fechaEnvio;

    @Column(name = "intentos", nullable = false)
    private int intentos;

    protected Notificacion() {
        // requerido por JPA
    }

    public Notificacion(Cita cita, TipoNotificacion tipo, String destinatario, String asunto,
                        LocalDateTime fechaProgramada) {
        this.cita = cita;
        this.tipo = tipo;
        this.destinatario = destinatario;
        this.asunto = asunto;
        this.fechaProgramada = fechaProgramada;
    }

    /** Registra un envío exitoso. */
    public void marcarEnviada(LocalDateTime ahora) {
        intentos++;
        estado = EstadoNotificacion.ENVIADO;
        fechaEnvio = ahora;
    }

    /** Registra un intento fallido; tras el máximo de intentos queda como FALLIDO. */
    public void registrarFallo(int maxIntentos) {
        intentos++;
        if (intentos >= maxIntentos) {
            estado = EstadoNotificacion.FALLIDO;
        }
    }

    public Long getId() {
        return id;
    }

    public Cita getCita() {
        return cita;
    }

    public TipoNotificacion getTipo() {
        return tipo;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public String getAsunto() {
        return asunto;
    }

    public EstadoNotificacion getEstado() {
        return estado;
    }

    public LocalDateTime getFechaProgramada() {
        return fechaProgramada;
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public int getIntentos() {
        return intentos;
    }
}
