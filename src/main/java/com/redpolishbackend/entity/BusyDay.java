package com.redpolishbackend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "diasOcupados")
public class BusyDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_ocupada", nullable = false)
    private LocalDate date;

    @Column(name = "hora_ocupada", nullable = false)
    private LocalTime hour;
}
