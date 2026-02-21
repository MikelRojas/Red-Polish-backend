package com.redpolishbackend.controller;

import com.redpolishbackend.dto.CitaDto;
import com.redpolishbackend.dto.CompraDto;
import com.redpolishbackend.dto.PayDto;
import com.redpolishbackend.entity.Cita;
import com.redpolishbackend.entity.Compra;
import com.redpolishbackend.entity.ShoppingCart;
import com.redpolishbackend.entity.User;
import com.redpolishbackend.repository.ProductRepository;
import com.redpolishbackend.repository.ShoppingCartRepository;
import com.redpolishbackend.service.*;
import com.redpolishbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments/sinpe")
@RequiredArgsConstructor
public class SinpeController {

    private final SinpeService sinpeService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final NotificacionService notificacionService;
    private final ShoppingCartRepository shoppingCartRepository;
    private final ShoppingCartRepository cartRepository;
    private final ShoppingCartService shoppingCartService;
    private final ProductRepository productRepository;

    @PostMapping("/pay/compra/{email}")
    public ResponseEntity<PayDto> checkoutSinpeCompra(
            @PathVariable String email,
            @RequestBody CompraDto compraDto,
            @RequestHeader("Authorization") String tokenHeader
    ) {
        String jwtToken = tokenHeader.startsWith("Bearer ")
                ? tokenHeader.substring(7)
                : tokenHeader;

        if (!jwtService.isTokenValid(jwtToken, email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            PayDto payDto = sinpeService.createPaymentCompra(compraDto, email);

            return ResponseEntity.ok(payDto);

        } catch (IllegalStateException | IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(PayDto.builder()
                            .id_compra(null)
                            .status("Failed")
                            .message(ex.getMessage())
                            .build()
                    );

        } catch (Exception ex) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(PayDto.builder()
                            .id_compra(null)
                            .status("Error")
                            .message("Error interno: " + ex.getMessage())
                            .build()
                    );
        }
    }

    @PostMapping("/pay/cita/{email}")
    public ResponseEntity<PayDto> checkoutSinpeCita(
            @PathVariable("email") String email,
            @RequestBody CitaDto citaDto,
            @RequestHeader("Authorization") String tokenHeader
    ) {
        String jwtToken = tokenHeader.startsWith("Bearer ")
                ? tokenHeader.substring(7)
                : tokenHeader;
        if (!jwtService.isTokenValid(jwtToken, email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            PayDto payDto = sinpeService.createPaymentCita(citaDto, email);
            return ResponseEntity.ok(payDto);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(PayDto.builder()
                            .status("PENDIENTE")
                            .message(null)
                            .sessionId(null)
                            .sessionUrl(null)
                            .build());
        }
    }



    @PutMapping("/confirm/compra/{compraId}")
    public ResponseEntity<PayDto> confirmSinpeCompraByAdmin(
            @PathVariable("compraId") Long compraId,
            @RequestHeader("Authorization") String tokenHeader
    ) {
        String jwtToken = tokenHeader.startsWith("Bearer ")
                ? tokenHeader.substring(7)
                : tokenHeader;
        if (!isAdmin(jwtToken)) {
            PayDto dto = PayDto.builder()
                    .id_compra(compraId)
                    .status("CONFIRMADA")
                    .message(null)
                    .sessionId(null)
                    .sessionUrl(null)
                    .build();
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(dto);
        }
        try {
            Compra pagoActualizado = sinpeService.actualizarEstadoPagoCompra(
                    compraId, "CONFIRMADA"
            );

            PayDto dto = PayDto.builder()
                    .id_compra(pagoActualizado.getIdCompra())
                    .status("CONFIRMADA")
                    .message(null)
                    .sessionId(null)
                    .sessionUrl(null)
                    .build();

            Compra buy = sinpeService.getCompraById(compraId);
            User user = buy.getCliente();
            List<ShoppingCart> productosComprados = shoppingCartRepository.findByUser(user);

            if (productosComprados.isEmpty()) {
                throw new IllegalStateException("No se encontraron productos para el usuario en el carrito.");
            }
            shoppingCartService.reducirStockDeProductos(productosComprados);

            StringBuilder detalleProductos = new StringBuilder();
            double total = 0;

            for (ShoppingCart item : productosComprados) {
                double precio = item.getProduct().getPromotion() != null
                        ? item.getProduct().getPrice() - (item.getProduct().getPrice() * item.getProduct().getPromotion().getPorcentage() / 100)
                        : item.getProduct().getPrice();

                double subtotal = precio * item.getQuantity();
                total += subtotal;

                detalleProductos.append(String.format("- %s x%d - $%.2f\n", item.getProduct().getName(), item.getQuantity(), subtotal));
            }

            String cuerpo = String.format("""
            Estimado/a %s,

            Gracias por su compra en RedPolish. A continuación encontrará los detalles de su factura:

            🧾 Detalles de la Compra
            %s
            Total pagado: $%.2f
            Estado del pago: CONFIRMADO

            📍 Dirección del local: RedPolish, Cedral, Ciudad Quesada, Costa Rica  
            📞 Contacto: +506 8358-2929  

            Esperamos que disfrute sus productos. ¡Gracias por confiar en nosotros!

            Atentamente,  
            Equipo RedPolish
            """,
                    user.getName(),
                    detalleProductos.toString(),
                    total
            );
            cartRepository.deleteAll(productosComprados);
            notificacionService.enviarCorreo(user.getEmail(), "Factura de compra y confirmación de pago", cuerpo);


            return ResponseEntity.ok(dto);

        } catch (IllegalArgumentException ex) {
            PayDto dto = PayDto.builder()
                    .id_compra(compraId)
                    .status("PENDIENTE")
                    .message(null)
                    .sessionId(null)
                    .sessionUrl(null)
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(dto);
        } catch (Exception ex) {
            PayDto dto = PayDto.builder()
                    .id_compra(compraId)
                    .status("PENDIENTE")
                    .message(null)
                    .sessionId(null)
                    .sessionUrl(null)
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(dto);
        }
    }


    @PutMapping("/confirm/cita/{citaId}")
    public ResponseEntity<PayDto> confirmSinpeCitaByAdmin(
            @PathVariable("citaId") Long citaId,
            @RequestHeader("Authorization") String tokenHeader
    ) {
        String jwtToken = tokenHeader.startsWith("Bearer ")
                ? tokenHeader.substring(7)
                : tokenHeader;
        if (!isAdmin(jwtToken)) {
            PayDto dto = PayDto.builder()
                    .id_compra(citaId)
                    .status(null)
                    .message(null)
                    .sessionId(null)
                    .sessionUrl(null)
                    .build();
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(dto);
        }

        try {
            Cita pagoActualizado = sinpeService.actualizarEstadoPagoCita(
                    citaId, "CONFIRMADA"
            );

            PayDto dto = PayDto.builder()
                    .id_compra(pagoActualizado.getId())
                    .status("CONFIRMADA")
                    .message(null)
                    .sessionId(null)
                    .sessionUrl(null)
                    .build();

            Cita appointment = sinpeService.getCitaById(citaId);
            User user = appointment.getUser();

            notificacionService.enviarCorreo(
                    user.getEmail(),
                    "Confirmación de Cita",
                    "Su cita ha sido confirmada exitosamente."
            );

            double precio = appointment.getService().getPromotion() != null
                    ? appointment.getService().getPrice() - (appointment.getService().getPrice() * appointment.getService().getPromotion().getPorcentage() / 100)
                    : appointment.getService().getPrice();

            String cuerpo = String.format("""
            Estimado/a %s,
        
            Gracias por agendar su cita con RedPolish. A continuación encontrará los detalles de su pago y confirmación de cita:
        
            🧾 Factura de Pago
            - Servicio: %s
            - Fecha: %s
            - Hora: %s
            - Precio: $%.2f
            - Estado: CONFIRMADA
        
            📍 Dirección del local: RedPolish, Cedral, Ciudad Quesada, Costa Rica  
            📞 Contacto: +506 8358-2929   
        
            Gracias por confiar en nosotros. Le esperamos en su cita.
        
            Atentamente,  
            Equipo RedPolish
            """,
                    appointment.getUser().getName(),
                    appointment.getService().getName(),
                    appointment.getDate().toString(),
                    appointment.getHour(),
                    precio
            );

            notificacionService.enviarCorreo(appointment.getUser().getEmail(),"Factura de pago y confirmacion de cita",cuerpo);

            return ResponseEntity.ok(dto);

        } catch (IllegalArgumentException ex) {
            PayDto dto = PayDto.builder()
                    .id_compra(citaId)
                    .status("PENDIENTE")
                    .message(null)
                    .sessionId(null)
                    .sessionUrl(null)
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(dto);
        } catch (Exception ex) {
            PayDto dto = PayDto.builder()
                    .id_compra(citaId)
                    .status("PENDIENTE")
                    .message(null)
                    .sessionId(null)
                    .sessionUrl(null)
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(dto);
        }
    }

    private boolean isAdmin(String token) {
        try {
            String email = jwtService.extractEmail(token);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            return "Administrador".equalsIgnoreCase(user.getRol());
        } catch (Exception e) {
            return false;
        }
    }
}
