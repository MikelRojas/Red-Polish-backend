package com.redpolishbackend.controller;

import com.redpolishbackend.dto.PromotionDto;
import com.redpolishbackend.entity.User;
import com.redpolishbackend.repository.UserRepository;
import com.redpolishbackend.service.JwtService;
import com.redpolishbackend.service.PromotionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;

//To update
@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<PromotionDto>> getAllPromotions() {
        return ResponseEntity.ok(promotionService.getAll());
    }

    @PostMapping
    public ResponseEntity<?> createPromotion(@RequestBody PromotionDto promotionDto,
                                             HttpServletRequest request) {
        if (!isAdmin(request)) {
            return ResponseEntity.status(403).body("Acceso denegado: Solo los administradores pueden crear promociones.");
        }
        return ResponseEntity.ok(promotionService.create(promotionDto));
    }


    @PutMapping("/{id}")
    public ResponseEntity<?> updatePromotion(@PathVariable Long id,
                                             @RequestBody PromotionDto promotionDto,
                                             HttpServletRequest request) {
        if (!isAdmin(request)) {
            return ResponseEntity.status(403).body("Acceso denegado: Solo los administradores pueden modificar promociones.");
        }
        return ResponseEntity.ok(promotionService.update(id, promotionDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePromotion(@PathVariable Long id,
                                             HttpServletRequest request) {
        if (!isAdmin(request)) {
            return ResponseEntity.status(403).body("Acceso denegado: Solo los administradores pueden eliminar promociones.");
        }
        promotionService.delete(id);
        return ResponseEntity.ok("Promoción eliminada correctamente.");
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

    @PostMapping("/send/{id}")
    public ResponseEntity<String> sendEmailPromotion(
            @PathVariable Long id,
            HttpServletRequest request) {

        if (!isAdmin(request)) {
            return ResponseEntity.status(403).body("Acceso denegado: Solo los administradores pueden enviar promociones.");
        }

        try {
            promotionService.sendEmailActivePromotions(id);
            return ResponseEntity.ok("Correos enviados para la promoción ID: " + id);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al enviar correos: " + e.getMessage());
        }
    }

    public void promotionsExpiration(){

    }
}
