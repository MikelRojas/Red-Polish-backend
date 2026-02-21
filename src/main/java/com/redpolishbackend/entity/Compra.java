package com.redpolishbackend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@Entity
@Table(name = "compras")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_compra")
    private Long idCompra;

    @Column(name = "descripcion", nullable = false)
    private String descripcion;

    @Column(name = "fecha_compra", nullable = false)
    private LocalDate fechaCompra = LocalDate.now();

    @ManyToOne
    @JoinColumn(name = "id_cliente")
    private User cliente;

    @Column(name = "precio_compra")
    private double precioCompra;

    @Column(name = "estado_pago", nullable = false)
    private String estadoPago = "PENDIENTE";
}
