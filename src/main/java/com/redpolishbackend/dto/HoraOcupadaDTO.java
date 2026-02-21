package com.redpolishbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
public class HoraOcupadaDTO {
    private LocalDate fecha;
    private LocalTime hora;
    private String duracion; // Minutos
}
