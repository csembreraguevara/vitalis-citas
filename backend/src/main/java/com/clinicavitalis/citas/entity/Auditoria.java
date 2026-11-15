package com.clinicavitalis.citas.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Registro de accesos y operaciones sobre datos personales
 * (tabla {@code auditoria}, RF018 y Ley N.° 29733).
 */
@Entity
@Table(name = "auditoria")
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_auditoria")
    private Long id;

    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(name = "accion", nullable = false, length = 40)
    private String accion;

    @Column(name = "entidad", length = 40)
    private String entidad;

    @Column(name = "id_entidad")
    private Long idEntidad;

    @Column(name = "detalle", length = 500)
    private String detalle;

    @Column(name = "ip", length = 45)
    private String ip;

    @Column(name = "fecha", insertable = false, updatable = false)
    private LocalDateTime fecha;

    protected Auditoria() {
        // requerido por JPA
    }

    public Auditoria(Long idUsuario, String accion, String entidad, Long idEntidad, String detalle, String ip) {
        this.idUsuario = idUsuario;
        this.accion = accion;
        this.entidad = entidad;
        this.idEntidad = idEntidad;
        this.detalle = detalle;
        this.ip = ip;
    }

    public Long getId() {
        return id;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public String getAccion() {
        return accion;
    }

    public String getEntidad() {
        return entidad;
    }

    public Long getIdEntidad() {
        return idEntidad;
    }

    public String getDetalle() {
        return detalle;
    }

    public String getIp() {
        return ip;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }
}
