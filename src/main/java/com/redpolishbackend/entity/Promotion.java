package com.redpolishbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Promociones")
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "titulo", nullable = false)
    private String title;

    @Column(name = "descripcion", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "porcentaje", nullable = false)
    private Double porcentage;

    @Column(name = "fecha_inicio", nullable = false)
    private Date start_date;

    @Column(name = "fecha_fin", nullable = false)
    private Date end_date;
}