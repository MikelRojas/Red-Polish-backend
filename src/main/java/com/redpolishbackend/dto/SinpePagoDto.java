package com.redpolishbackend.dto;


import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SinpePagoDto {
    private Long id;
    private BigDecimal monto;
    private String referencia;
    private LocalDate fecha;
    // Archivo del comprobante
    private Long citaId;
}
