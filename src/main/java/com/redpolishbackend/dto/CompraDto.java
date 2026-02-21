package com.redpolishbackend.dto;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompraDto {
    private Long idCompra;
    private String descripcion;
    private LocalDate fechaCompra;
    private String usuarioEmail;
    private String estadoPago;
}