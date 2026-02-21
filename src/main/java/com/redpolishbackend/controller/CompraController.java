package com.redpolishbackend.controller;

import com.redpolishbackend.dto.CompraDto;
import com.redpolishbackend.dto.ProductDto;
import com.redpolishbackend.entity.Compra;
import com.redpolishbackend.entity.User;
import com.redpolishbackend.repository.UserRepository;
import com.redpolishbackend.service.CompraService;
import com.redpolishbackend.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/compras")
@RequiredArgsConstructor
public class CompraController {

    private final CompraService compraService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @GetMapping("/history/{email}")
    public ResponseEntity<List<Compra>> getComprasUsuario(
            @RequestHeader("Authorization") String token,
            @PathVariable("email") String email
    ) {
        String jwtToken = token;
        if (token.startsWith("Bearer ")) {
            jwtToken = token.substring(7);
        }

        if (!jwtService.isTokenValid(jwtToken, email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<Compra> compras = compraService.obtenerComprasUsuarioEntities(user.getId());
        return ResponseEntity.ok(compras);
    }

    @GetMapping("/admin")
    public ResponseEntity<List<Compra>> getComprasAdmin(HttpServletRequest request) {
        if (!isAdmin(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<Compra> todas = compraService.obtenerComprasAdminEntities();
        return ResponseEntity.ok(todas);
    }

    private boolean isAdmin(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }
        String token = authHeader.substring(7);

        try {
            String email = jwtService.extractEmail(token);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            return "Administrador".equalsIgnoreCase(user.getRol());

        } catch (Exception e) {
            return false;
        }
    }

    @PutMapping("/update_state/{id}/{email}/{new_state}")
    public ResponseEntity<CompraDto> updateProduct(
            @PathVariable("email") String email,
            @PathVariable("id") Long id,
            @PathVariable("new_state") String new_state,
            @RequestHeader("Authorization") String token) {
        String jwtToken = token;
        if (token.startsWith("Bearer ")) {
            jwtToken = token.substring(7);
        }

        if (!jwtService.isTokenValid(jwtToken, email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        CompraDto updatedCompra = compraService.cambiarEstado(id, new_state);
        return ResponseEntity.ok(updatedCompra);
    }
}
