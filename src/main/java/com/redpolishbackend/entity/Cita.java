package com.redpolishbackend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "Cita")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Cita {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha", nullable = false)
    private LocalDate date;

    @Column(name = "hora", nullable = false)
    private LocalTime hour;

    @Column(name = "estado", nullable = false)
    private String state; // "PENDIENTE", "CONFIRMADA", "CANCELADA EXITOSA", "CANCELADA PENDIENTE"

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "servicio_id", referencedColumnName = "id")
    private Services service;
}