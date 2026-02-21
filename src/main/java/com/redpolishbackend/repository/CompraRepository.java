package com.redpolishbackend.repository;

import com.redpolishbackend.entity.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {
    List<Compra> findByClienteIdAndFechaCompraAfter(Long clienteId, LocalDate fechaLimite);
    List<Compra> findByFechaCompraAfter(LocalDate fechaLimite);
    Optional<Compra> findByFechaCompraAndClienteIdAndPrecioCompraAndEstadoPago(LocalDate fechaCompra, Long cliente_id, double precioCompra, String estadoPago);
    @Modifying
    @Query("DELETE FROM Compra c WHERE c.fechaCompra < :fechaLimite")
    int deleteOldBuys(LocalDate fechaLimite);
    boolean existsByClienteIdAndEstadoPago(Long clienteId, String estadoPago);
}
