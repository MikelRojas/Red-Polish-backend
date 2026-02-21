package com.redpolishbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Servicios")
public class Services {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="nombre", nullable = false)
    private String name;

    @Column(name="descripcion", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name="duracion", nullable = false)
    private String duration;

    @Column(name="precio", nullable = false)
    private Double price;

    @Column(name="imagen_url", nullable = false)
    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "id_promocion")
    private Promotion promotion;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Category category;
}