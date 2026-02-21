package com.redpolishbackend.mapper;

import com.redpolishbackend.dto.CompraDto;
import com.redpolishbackend.dto.ProductDto;
import com.redpolishbackend.entity.*;
import com.redpolishbackend.service.CompraService;
import com.redpolishbackend.service.ProductService;
import com.redpolishbackend.service.UserService;
import org.springframework.stereotype.Service;

public class CompraMapper {


    public static CompraDto toDto(Compra compra) {
        CompraDto dto = new CompraDto(
                compra.getIdCompra(),
                compra.getDescripcion(),
                compra.getFechaCompra(),
                compra.getCliente().getEmail(),
                compra.getEstadoPago()
        );
        return dto;
    }

    public static Compra mapToCompra(CompraDto dto, User user, double precio) {
        return new Compra(
                dto.getIdCompra(),
                dto.getDescripcion(),
                dto.getFechaCompra(),
                user,
                precio,
                dto.getEstadoPago()
        );
    }
}
