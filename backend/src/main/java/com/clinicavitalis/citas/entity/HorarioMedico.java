package com.clinicavitalis.citas.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Bloque de atención semanal de un médico (tabla {@code horario_medico}).
 * El día se guarda como número: 1 = lunes … 7 = domingo (igual que ISO-8601).
 */
@Entity
@Table(name = "horario_medico")
public class HorarioMedico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_horario")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_medico", nullable = false)
    private Medico medico;

    @Column(name = "dia_semana", nullable = false)
    private int diaSemana;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    protected HorarioMedico() {
        // requerido por JPA
    }

    public HorarioMedico(Medico medico, DayOfWeek dia, LocalTime horaInicio, LocalTime horaFin) {
        this.medico = medico;
        this.diaSemana = dia.getValue();
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }

    /** Día de la semana como {@link DayOfWeek} (más fácil de comparar con una fecha). */
    public DayOfWeek getDia() {
        return DayOfWeek.of(diaSemana);
    }

    /** Indica si una hora cae dentro del bloque (inicio incluido, fin excluido). */
    public boolean incluye(LocalTime hora) {
        return !hora.isBefore(horaInicio) && hora.isBefore(horaFin);
    }

    public Long getId() {
        return id;
    }

    public Medico getMedico() {
        return medico;
    }

    public int getDiaSemana() {
        return diaSemana;
    }

    public void setDia(DayOfWeek dia) {
        this.diaSemana = dia.getValue();
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
