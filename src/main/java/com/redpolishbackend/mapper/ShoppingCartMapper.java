package com.redpolishbackend.mapper;

import com.redpolishbackend.dto.ShoppingCartDto;
import com.redpolishbackend.entity.Product;
import com.redpolishbackend.entity.ShoppingCart;
import com.redpolishbackend.entity.User;

public class ShoppingCartMapper {

    public static ShoppingCartDto mapToShoppingCartDto(ShoppingCart cart) {
        return new ShoppingCartDto(
                cart.getId(),
                cart.getUser() != null ? cart.getUser().getId() : null,
                cart.getProduct() != null ? cart.getProduct().getId() : null,
                cart.getQuantity()
        );
    }

    public static ShoppingCart mapToShoppingCart(ShoppingCartDto dto, User user, Product product) {
        return new ShoppingCart(
                dto.getId(),
                user,
                product,
                dto.getQuantity()
        );
    }
}