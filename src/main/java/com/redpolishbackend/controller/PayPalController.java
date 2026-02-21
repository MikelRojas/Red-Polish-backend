package com.redpolishbackend.controller;

import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import com.redpolishbackend.dto.CitaDto;
import com.redpolishbackend.dto.CompraDto;
import com.redpolishbackend.dto.PayDto;
import com.redpolishbackend.entity.AppointmentPivot;
import com.redpolishbackend.entity.PaymentPivot;
import com.redpolishbackend.repository.UserRepository;
import com.redpolishbackend.service.CitaService;
import com.redpolishbackend.service.JwtService;
import com.redpolishbackend.service.PaypalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PayPalController {

    private final PaypalService paypalService;
    private final JwtService jwtService;
    private final CitaService appointmentService;

    @PostMapping("/pay/{email}")
    public ResponseEntity<PayDto> checkoutProducts(
            @PathVariable("email") String email,
            @RequestBody CompraDto compraDto,
            @RequestHeader("Authorization") String token
    ) {
        String jwtToken = token;
        if (token.startsWith("Bearer ")) {
            jwtToken = token.substring(7);
        }

        if (!jwtService.isTokenValid(jwtToken, email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            PaymentPivot paymentPivot = paypalService.createPaymentProducts(compraDto);
            for(Links links : paymentPivot.getPayment().getLinks()){
                if(links.getRel().equals("approval_url")){
                    return ResponseEntity.ok(PayDto.builder()
                            .status("Success")
                            .message("Payment session created")
                            .sessionId(paymentPivot.getPayment().getId())
                            .sessionUrl(links.getHref())
                            .id_compra(paymentPivot.getCompra().getIdCompra())
                            .build());
                }
            }
        } catch (PayPalRESTException e) {
            throw new RuntimeException(e);
        }
        return null;
    }


    @PostMapping("/pay/appointment/{email}")
    public ResponseEntity<PayDto> checkoutCita(
            @PathVariable("email") String email,
            @RequestBody CitaDto citaDto,
            @RequestHeader("Authorization") String token
    ) {
        String jwtToken = token;
        if (token.startsWith("Bearer ")) {
            jwtToken = token.substring(7);
        }

        if (!jwtService.isTokenValid(jwtToken, email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            AppointmentPivot appointmentPivot = paypalService.createPaymentAppointment(citaDto);

            for (Links links : appointmentPivot.getPayment().getLinks()) {
                if (links.getRel().equals("approval_url")) {
                    return ResponseEntity.ok(PayDto.builder()
                            .status("Success")
                            .message("Payment session created for appointment")
                            .sessionId(appointmentPivot.getPayment().getId())
                            .sessionUrl(links.getHref())
                            .id_compra(appointmentPivot.getCita().getId()) // aquí sí hay una cita creada
                            .build());
                }
            }
        } catch (PayPalRESTException e) {
            throw new RuntimeException("Error creando el pago con PayPal", e);
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    /*
    @PostMapping("/success/{email}")
    public ResponseEntity<PayDto> paymentSuccess(
            @PathVariable("email") String email,
            @RequestParam("paymentId") String paymentId,
            @RequestParam("PayerID") String payerId,
            @RequestHeader("Authorization") String token) {

        String jwtToken = token;
        if (token.startsWith("Bearer ")) {
            jwtToken = token.substring(7);
        }

        if (!jwtService.isTokenValid(jwtToken, email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Payment payment = paypalService.execute(paymentId, payerId);
            if (payment.getState().equalsIgnoreCase("approved")) {
                return ResponseEntity.ok(PayDto.builder()
                        .status("Success")
                        .message("Payment approved")
                        .sessionId(payment.getId())
                        .build());
            }
        } catch (PayPalRESTException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(PayDto.builder()
                    .status("Error")
                    .message("Error al procesar el pago: " + e.getMessage())
                    .build());
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(PayDto.builder()
                .status("Failed")
                .message("Payment not approved")
                .build());
    }*/

    @PostMapping("/success/appointment/{id}/{email}")
    public ResponseEntity<PayDto> paymentAppointmentSuccess(
            @PathVariable("email") String email,
            @PathVariable("id") Long appointmentId,
            @RequestParam("paymentId") String paymentId,
            @RequestParam("PayerID") String payerId,
            @RequestHeader("Authorization") String token) {

        String jwtToken = token;
        if (token.startsWith("Bearer ")) {
            jwtToken = token.substring(7);
        }

        if (!jwtService.isTokenValid(jwtToken, email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Payment payment = paypalService.executeAppointment(paymentId, payerId,appointmentId);
            if (payment.getState().equalsIgnoreCase("approved")) {
                return ResponseEntity.ok(PayDto.builder()
                        .status("Success")
                        .message("Payment approved")
                        .sessionId(payment.getId())
                        .build());
            }
        } catch (PayPalRESTException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(PayDto.builder()
                    .status("Error")
                    .message("Error al procesar el pago: " + e.getMessage())
                    .build());
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(PayDto.builder()
                .status("Failed")
                .message("Payment not approved")
                .build());
    }

    @PostMapping("/success/buy/{id}/{email}")
    public ResponseEntity<PayDto> paymentBuySuccess(
            @PathVariable("email") String email,
            @PathVariable("id") Long buyId,
            @RequestParam("paymentId") String paymentId,
            @RequestParam("PayerID") String payerId,
            @RequestHeader("Authorization") String token) {

        String jwtToken = token;
        if (token.startsWith("Bearer ")) {
            jwtToken = token.substring(7);
        }

        if (!jwtService.isTokenValid(jwtToken, email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Payment payment = paypalService.executeProducts(paymentId, payerId,buyId);
            if (payment.getState().equalsIgnoreCase("approved")) {
                return ResponseEntity.ok(PayDto.builder()
                        .status("Success")
                        .message("Payment approved")
                        .sessionId(payment.getId())
                        .build());
            }
        } catch (PayPalRESTException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(PayDto.builder()
                    .status("Error")
                    .message("Error al procesar el pago: " + e.getMessage())
                    .build());
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(PayDto.builder()
                .status("Failed")
                .message("Payment not approved")
                .build());
    }
}