package com.redpolishbackend.service;

import com.redpolishbackend.dto.ShoppingCartDto;
import com.redpolishbackend.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService {
    ShoppingCartDto addProductToCart(ShoppingCartDto shoppingCartDto);
    List<ShoppingCartDto> getCartItems(Long userId);
    ShoppingCartDto updateQuantity(Long itemId, int quantity);
    void removeProductFromCart(Long itemId);
    void clearCart(Long userId);
    void reducirStockDeProductos(List<ShoppingCart> items);
}
