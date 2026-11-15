package com.clinicavitalis.citas.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Especialidad médica que ofrece la clínica (tabla {@code especialidad}).
 * La duración define el tamaño de cada turno al calcular la disponibilidad.
 */
@Entity
@Table(name = "especialidad")
public class Especialidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_especialidad")
    private Long id;

    @Column(name = "nombre", nullable = false, unique = true, length = 60)
    private String nombre;

    @Column(name = "descripcion", length = 250)
    private String descripcion;

    @Column(name = "duracion_cita_min", nullable = false)
    private int duracionCitaMin = 30;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    protected Especialidad() {
        // requerido por JPA
    }

    public Especialidad(String nombre, String descripcion, int duracionCitaMin) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.duracionCitaMin = duracionCitaMin;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getDuracionCitaMin() {
        return duracionCitaMin;
    }

    public void setDuracionCitaMin(int duracionCitaMin) {
        this.duracionCitaMin = duracionCitaMin;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
