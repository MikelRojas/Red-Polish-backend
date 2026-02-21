package com.redpolishbackend.service.impl;

import com.redpolishbackend.dto.CompraDto;
import com.redpolishbackend.entity.Compra;
import com.redpolishbackend.exception.ResourceNotFoundException;
import com.redpolishbackend.mapper.CompraMapper;
import com.redpolishbackend.repository.CompraRepository;
import com.redpolishbackend.service.CompraService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompraServiceImpl implements CompraService {

    private final CompraRepository compraRepository;
    private static final Logger logger = Logger.getLogger(CompraServiceImpl.class.getName());

    @Override
    public List<CompraDto> obtenerComprasUsuario(Long usuarioId) {
        LocalDate fechaLimite = LocalDate.now().minusMonths(3);
        return compraRepository.findByClienteIdAndFechaCompraAfter(usuarioId, fechaLimite)
                .stream()
                .map(CompraMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CompraDto> obtenerComprasAdmin() {
        LocalDate fechaLimite = LocalDate.now().minusMonths(3);
        return compraRepository.findByFechaCompraAfter(fechaLimite)
                .stream()
                .map(CompraMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CompraDto cambiarEstado(Long idCompra, String nuevoEstado) {
        Compra existingCompra = compraRepository.findById(idCompra)
                .orElseThrow(() -> new ResourceNotFoundException("Compra con id " + idCompra + " no encontrado"));

        existingCompra.setEstadoPago(nuevoEstado);

        Compra updatedCompra = compraRepository.save(existingCompra);
        return CompraMapper.toDto(updatedCompra);
    }

    @Override
    public List<Compra> obtenerComprasUsuarioEntities(Long usuarioId) {
        LocalDate fechaLimite = LocalDate.now().minusMonths(3);
        return compraRepository.findByClienteIdAndFechaCompraAfter(usuarioId, fechaLimite);
    }

    @Override
    public List<Compra> obtenerComprasAdminEntities() {
        return compraRepository.findAll();
    }

    @Scheduled(cron = "0 0 0 * * ?") // Todos los días a la medianoche
    @Transactional
    public void eliminarComprasAntiguas() {
        LocalDate fechaLimite = LocalDate.now().minusMonths(2);
        int eliminadas = compraRepository.deleteOldBuys(fechaLimite);
        logger.info("Se eliminaron " + eliminadas + " compras con más de 2 meses de antigüedad.");
    }
}
