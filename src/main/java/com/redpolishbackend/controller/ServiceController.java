package com.redpolishbackend.controller;

import com.redpolishbackend.dto.PromotionDto;
import com.redpolishbackend.dto.ServiceDTO;
import com.redpolishbackend.service.JwtService;
import com.redpolishbackend.service.ServicesService;
import com.redpolishbackend.service.impl.PromotionServiceImpl;
import com.redpolishbackend.service.impl.ServicesImpl;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@RestController
@RequestMapping("/api/services")
public class ServiceController {

    private final ServicesImpl servicesService;
    private final JwtService jwtService;
    private final PromotionServiceImpl promotionService;

    // Crear nuevo servicio
    @PostMapping("/add/{email}")
    public ResponseEntity<ServiceDTO> createService(
            @PathVariable("email") String email,
            @RequestBody ServiceDTO serviceDTO,
            @RequestHeader("Authorization") String token) {
        String jwtToken = token.startsWith("Bearer ") ? token.substring(7) : token;

        if (!jwtService.isTokenValid(jwtToken, email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        System.out.println(serviceDTO);
        ServiceDTO savedService = servicesService.createService(serviceDTO);
        return new ResponseEntity<>(savedService, HttpStatus.CREATED);
    }

    // Obtener todos los servicios
    @GetMapping("/get_all")
    public ResponseEntity<List<ServiceDTO>> getAllServices() {
        List<ServiceDTO> services = servicesService.getServices();
        List<ServiceDTO> discounted = services.stream()
                .map(this::applyDiscount)
                .collect(Collectors.toList());
        return ResponseEntity.ok(discounted);
    }

    // Obtener servicio por ID
    @GetMapping("/get/{id}")
    public ResponseEntity<ServiceDTO> getServiceById(@PathVariable Long id) {
        ServiceDTO service = servicesService.getServiceById(id);
        return ResponseEntity.ok(applyDiscount(service));
    }

    // Eliminar servicio por ID
    @DeleteMapping("/delete/{id}/{email}")
    public ResponseEntity<Void> deleteService(
            @PathVariable Long id,
            @PathVariable("email") String email,
            @RequestHeader("Authorization") String token) {
        String jwtToken = token.startsWith("Bearer ") ? token.substring(7) : token;

        if (!jwtService.isTokenValid(jwtToken, email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        System.out.println("ID recibido: " + id + " (tipo: " + id.getClass().getName() + ")");

        servicesService.deleteService(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // Actualizar servicio por ID
    @PutMapping("/update/{id}/{email}")
    public ResponseEntity<ServiceDTO> updateService(
            @PathVariable("id") Long id,
            @PathVariable("email") String email,
            @RequestBody ServiceDTO serviceDTO,
            @RequestHeader("Authorization") String token) {
        String jwtToken = token.startsWith("Bearer ") ? token.substring(7) : token;

        if (!jwtService.isTokenValid(jwtToken, email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        ServiceDTO updatedService = servicesService.updateService(id, serviceDTO);
        return ResponseEntity.ok(updatedService);
    }

    private ServiceDTO applyDiscount(ServiceDTO original) {
        ServiceDTO copy = new ServiceDTO(original);
        if (copy.getPromotionId() != null) {
            PromotionDto promotion = promotionService.getById(copy.getPromotionId());
            double discount = promotion.getPorcentage();
            double discountedPrice = copy.getPrice() * (1 - discount / 100.0);
            copy.setPrice(discountedPrice);
        }
        return copy;
    }
}