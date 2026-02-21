package com.redpolishbackend.controller;

import com.redpolishbackend.dto.CompraDto;
import com.redpolishbackend.dto.PayDto;
import com.redpolishbackend.service.JwtService;
import com.redpolishbackend.service.StripeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/llala")
@RequiredArgsConstructor
public class ProductCheckoutController {

    private final StripeService stripeService;
    private final JwtService jwtService;

    @PostMapping("/buy/{email}")
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
        PayDto payDto = stripeService.checkoutProducts(compraDto);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(payDto);
    }


}
