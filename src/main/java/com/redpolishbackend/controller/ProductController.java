package com.redpolishbackend.controller;

import com.redpolishbackend.dto.ProductDto;
import com.redpolishbackend.dto.PromotionDto;
import com.redpolishbackend.service.AuthService;
import com.redpolishbackend.service.JwtService;
import com.redpolishbackend.service.impl.ProductServiceImpl;
import com.redpolishbackend.service.impl.PromotionServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductServiceImpl productService;
    private JwtService jwtService;
    private PromotionServiceImpl promotionService;

    @PostMapping("/create/{email}")
    public ResponseEntity<ProductDto> createProduct(
            @PathVariable("email") String email,
            @RequestBody ProductDto productDto,
            @RequestHeader("Authorization") String token) {
        String jwtToken = token;
        if (token.startsWith("Bearer ")) {
            jwtToken = token.substring(7);
        }

        if (!jwtService.isTokenValid(jwtToken, email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        ProductDto savedProduct = productService.createProduct(productDto);
        return new ResponseEntity<>(savedProduct, HttpStatus.CREATED);
    }

    @GetMapping("/get_all")
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        List<ProductDto> products = productService.getProducts();
        List<ProductDto> result = products.stream()
                .map(this::applyDiscount)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long id) {
        ProductDto product = productService.getProductById(id);
        ProductDto discounted = applyDiscount(product);
        return ResponseEntity.ok(discounted);
    }

    @DeleteMapping("/delete/{id}/{email}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable("email") String email,
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        String jwtToken = token;
        if (token.startsWith("Bearer ")) {
            jwtToken = token.substring(7);
        }

        if (!jwtService.isTokenValid(jwtToken, email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        productService.deleteProduct(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // Actualizar producto por ID
    @PutMapping("/update/{id}/{email}")
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable("email") String email,
            @PathVariable("id") Long id,
            @RequestBody ProductDto productDto,
            @RequestHeader("Authorization") String token) {
        String jwtToken = token;
        if (token.startsWith("Bearer ")) {
            jwtToken = token.substring(7);
        }

        if (!jwtService.isTokenValid(jwtToken, email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        System.out.println(productDto);
        ProductDto updatedProduct = productService.updateProduct(id, productDto);
        return ResponseEntity.ok(updatedProduct);
    }

    private ProductDto applyDiscount(ProductDto original) {
        ProductDto copy = new ProductDto(original);
        if (copy.getPromotionId() != null) {
            PromotionDto promotion = promotionService.getById(copy.getPromotionId());
            double discount = promotion.getPorcentage();
            double discountedPrice = copy.getPrice() * (1 - discount / 100.0);
            copy.setPrice(discountedPrice);
        }
        return copy;
    }
}