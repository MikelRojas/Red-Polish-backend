package com.redpolishbackend.controller;

import com.redpolishbackend.dto.ProductDto;
import com.redpolishbackend.dto.ShoppingCartDto;
import com.redpolishbackend.service.JwtService;
import com.redpolishbackend.service.ShoppingCartService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/cart")
public class ShoppingCartController {

    private final ShoppingCartService shoppingCartService;
    private final JwtService jwtService;

    private boolean isTokenValid(String token, String email) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtService.isTokenValid(token, email);
    }

    @PostMapping("/add/{email}")
    public ResponseEntity<ShoppingCartDto> addProductToCart(
            @RequestBody ShoppingCartDto shoppingCartDto,
            @PathVariable String email,
            @RequestHeader("Authorization") String token) {

        if (!isTokenValid(token, email)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();


        ShoppingCartDto savedItem = shoppingCartService.addProductToCart(shoppingCartDto);
        return new ResponseEntity<>(savedItem, HttpStatus.CREATED);
    }

    @GetMapping("/items/{userId}/{email}")
    public ResponseEntity<List<ShoppingCartDto>> getCartItems(
            @PathVariable Long userId,
            @PathVariable String email,
            @RequestHeader("Authorization") String token) {

        if (!isTokenValid(token, email)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        List<ShoppingCartDto> items = shoppingCartService.getCartItems(userId);
        return ResponseEntity.ok(items);
    }

    @PutMapping("/update/{itemId}/{quantity}/{email}")
    public ResponseEntity<ShoppingCartDto> updateQuantity(
            @PathVariable Long itemId,
            @PathVariable int quantity,
            @PathVariable String email,
            @RequestHeader("Authorization") String token) {

        if (!isTokenValid(token, email)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        ShoppingCartDto updatedItem = shoppingCartService.updateQuantity(itemId, quantity);
        return ResponseEntity.ok(updatedItem);
    }

    @DeleteMapping("/remove/{itemId}/{email}")
    public ResponseEntity<String> removeProductFromCart(
            @PathVariable Long itemId,
            @PathVariable String email,
            @RequestHeader("Authorization") String token) {

        if (!isTokenValid(token, email)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        shoppingCartService.removeProductFromCart(itemId);
        return ResponseEntity.ok("Eliminado correctamente");
    }

    @DeleteMapping("/clear/{userId}/{email}")
    public ResponseEntity<String> clearCart(
            @PathVariable Long userId,
            @PathVariable String email,
            @RequestHeader("Authorization") String token) {

        if (!isTokenValid(token, email)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        shoppingCartService.clearCart(userId);
        return ResponseEntity.ok("Carrito limpiado correctamente");
    }
}
