package com.redpolishbackend.service;

import com.redpolishbackend.dto.CitaDto;
import com.redpolishbackend.dto.CompraDto;
import com.redpolishbackend.dto.PayDto;
import com.redpolishbackend.entity.*;
import com.redpolishbackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SinpeService {

    private final CompraRepository compraRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final ShoppingCartRepository cartRepository;
    private final CitaRepository appointmentRepository;

    public Compra actualizarEstadoPagoCompra(
            Long compraId,
            String nuevoEstado
    ) {
        Compra buy = compraRepository.findById(compraId)
                .orElseThrow(() -> new IllegalArgumentException("No existe la compra con ID = " + compraId));
        if (!nuevoEstado.equals("CONFIRMADA") && !nuevoEstado.equals("PENDIENTE")) {
            throw new IllegalArgumentException("Estado inválido: " + nuevoEstado);
        }
        buy.setEstadoPago(nuevoEstado);

        return compraRepository.save(buy);
    }


    public Cita actualizarEstadoPagoCita(
            Long citaId,
            String nuevoEstado)
    {
        Cita cita = appointmentRepository.findById(citaId)
                .orElseThrow(() -> new IllegalArgumentException("No existe la cita con ID = " + citaId));

        if (!nuevoEstado.equals("CONFIRMADA") && !nuevoEstado.equals("PENDIENTE")
                && !nuevoEstado.equals("CANCELADA EXITOSA") && !nuevoEstado.equals("CANCELADA PENDIENTE")) {
            throw new IllegalArgumentException("Estado inválido: " + nuevoEstado);
        }
        Cita appointment = appointmentRepository.findById(citaId)
                .orElseThrow(() -> new IllegalArgumentException("No existe la cita con ID = " + citaId));
        appointment.setState(nuevoEstado);
        return appointmentRepository.save(cita);
    }

    public PayDto createPaymentCompra(CompraDto compraDto, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + email));

        if (compraRepository.existsByClienteIdAndEstadoPago(user.getId(), "PENDIENTE")) {
            throw new IllegalStateException("Ya existe una compra pendiente. Por favor finalícela antes de realizar otra.");
        }

        List<ShoppingCart> cartItems = cartRepository.findByUser(user);
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("El carrito está vacío");
        }

        double total = cartItems.stream()
                .mapToDouble(ci -> {
                    double price = ci.getProduct().getPrice();

                    Promotion promo = ci.getProduct().getPromotion();
                    if (promo != null) {
                        LocalDate today = LocalDate.now();
                        LocalDate start = promo.getStart_date().toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate();
                        LocalDate end = promo.getEnd_date().toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate();

                        // Verificamos si 'today' está en el rango [start, end]
                        if ((today.isEqual(start) || today.isAfter(start)) &&
                                (today.isEqual(end) || today.isBefore(end))) {
                            price *= (1 - promo.getPorcentage() / 100.0);
                        }
                    }

                    return price * ci.getQuantity();
                })
                .sum();


        Optional<Compra> existingCompraOpt = compraRepository.findByFechaCompraAndClienteIdAndPrecioCompraAndEstadoPago(
                compraDto.getFechaCompra(), user.getId(), total,"PENDIENTE");

        Compra compra = existingCompraOpt.orElseGet(Compra::new);
        compra.setDescripcion(compraDto.getDescripcion());
        compra.setFechaCompra(
                compraDto.getFechaCompra() != null
                        ? compraDto.getFechaCompra()
                        : LocalDate.now()
        );
        compra.setCliente(user);
        compra.setPrecioCompra(total);
        compra.setEstadoPago("PENDIENTE");
        Compra saved = compraRepository.save(compra);
        return PayDto.builder()
                .id_compra(saved.getIdCompra())
                .status("Pending")
                .message("Compra creada y carrito limpiado")
                .build();
    }

    public PayDto createPaymentCita(CitaDto citaDto, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + email));

        if (appointmentRepository.existsByUserIdAndState(user.getId(), "PENDIENTE")) {
            throw new IllegalStateException("Ya existe una cita pendiente. Debe confirmarla o cancelarla antes de crear otra.");
        }

        Services service = serviceRepository.findById(citaDto.getServiceId())
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado con ID: " + citaDto.getServiceId()));

        Optional<Cita> existingCitaOpt = appointmentRepository
                .findByDateAndHourAndUserIdAndServiceIdAndState(
                        citaDto.getDate(),
                        citaDto.getHour(),
                        citaDto.getUserId(),
                        citaDto.getServiceId(),
                        "PENDIENTE"
                );

        Cita cita = existingCitaOpt.orElse(new Cita());
        cita.setDate(citaDto.getDate());
        cita.setHour(citaDto.getHour());

        cita.setState("PENDIENTE");

        cita.setUser(user);
        cita.setService(service);

        Cita saved = appointmentRepository.save(cita);

        return PayDto.builder()
                .status("Success")
                .message("Cita registrada en modo PENDIENTE")
                .sessionId(null)
                .sessionUrl(null)
                .id_compra(saved.getId())
                .build();
    }

    public Cita getCitaById(Long citaId) {

        return appointmentRepository.findById(citaId)
                .orElseThrow(() -> new IllegalArgumentException("No existe la cita con ID = " + citaId));
    }

    public Compra getCompraById(Long compraId) {

        return compraRepository.findById(compraId)
                .orElseThrow(() -> new IllegalArgumentException("No existe la compra con ID = " + compraId));
    }
}
