package com.redpolishbackend.service;

import com.redpolishbackend.dto.CompraDto;
import com.redpolishbackend.entity.Compra;

import java.util.List;

public interface CompraService {
    List<CompraDto> obtenerComprasUsuario(Long usuarioId);
    List<CompraDto> obtenerComprasAdmin();
    CompraDto cambiarEstado(Long idCompra, String nuevoEstado);
    List<Compra> obtenerComprasUsuarioEntities(Long usuarioId);
    List<Compra> obtenerComprasAdminEntities();
}
