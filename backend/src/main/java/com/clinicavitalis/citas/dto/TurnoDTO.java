package com.clinicavitalis.citas.dto;

import java.time.LocalTime;

/** Un turno del médico en una fecha y si todavía se puede reservar. */
public record TurnoDTO(LocalTime hora, boolean disponible) {
}
