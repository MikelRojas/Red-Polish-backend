package com.redpolishbackend.service.impl;

import com.redpolishbackend.dto.ShoppingCartDto;
import com.redpolishbackend.entity.Product;
import com.redpolishbackend.entity.ShoppingCart;
import com.redpolishbackend.entity.User;
import com.redpolishbackend.exception.ResourceNotFoundException;
import com.redpolishbackend.mapper.ShoppingCartMapper;
import com.redpolishbackend.repository.ProductRepository;
import com.redpolishbackend.repository.ShoppingCartRepository;
import com.redpolishbackend.repository.UserRepository;
import com.redpolishbackend.service.ShoppingCartService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private final ShoppingCartRepository shoppingCartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    public ShoppingCartDto addProductToCart(ShoppingCartDto shoppingCartDto) {
        User user = userRepository.findById(shoppingCartDto.getUserId()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Product product = productRepository.findById(shoppingCartDto.getProductId()).orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        ShoppingCart item = shoppingCartRepository.findByUserAndProduct(user,product).orElse(null);
        if (product.getStock() <shoppingCartDto.getQuantity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock insuficiente para el producto: " + product.getName());

        }
        if(item != null) {
            if (product.getStock() < item.getQuantity() + 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock insuficiente para el producto: " + product.getName());
            }
            item.setQuantity(item.getQuantity() + 1);
            ShoppingCart savedShoppingCart = shoppingCartRepository.save(item);
            return ShoppingCartMapper.mapToShoppingCartDto(savedShoppingCart);
        }

        item = ShoppingCartMapper.mapToShoppingCart(shoppingCartDto,user,product);
        ShoppingCart savedShoppingCart = shoppingCartRepository.save(item);

        return ShoppingCartMapper.mapToShoppingCartDto(savedShoppingCart);
    }

    @Override
    public List<ShoppingCartDto> getCartItems(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return shoppingCartRepository.findByUser(user).stream()
                .map(ShoppingCartMapper::mapToShoppingCartDto)
                .collect(Collectors.toList());
    }

    @Override
    public ShoppingCartDto updateQuantity(Long itemId, int quantity) {
        if (quantity < 1) throw new IllegalArgumentException("Quantity must be at least 1");
        ShoppingCart item = shoppingCartRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not in cart"));
        if (item.getProduct().getStock() < item.getQuantity() + 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock insuficiente para el producto: " + item.getProduct().getName());
        }
        item.setQuantity(quantity);
        ShoppingCart savedShoppingCart = shoppingCartRepository.save(item);
        return ShoppingCartMapper.mapToShoppingCartDto(savedShoppingCart);
    }

    @Override
    public void removeProductFromCart(Long itemId) {
        ShoppingCart item = shoppingCartRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not in cart"));
        shoppingCartRepository.delete(item);
    }

    @Override
    public void clearCart(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        List<ShoppingCart> items = shoppingCartRepository.findByUser(user);
        shoppingCartRepository.deleteAll(items);
    }

    @Override
    public void reducirStockDeProductos(List<ShoppingCart> items) {
        for (ShoppingCart item : items) {
            Product producto = item.getProduct();
            int cantidadComprada = item.getQuantity();
            int stockActual = producto.getStock();

            if (stockActual < cantidadComprada) {
                throw new IllegalStateException("Stock insuficiente para el producto: " + producto.getName());
            }

            producto.setStock(stockActual - cantidadComprada);
            productRepository.save(producto);
        }
    }

}
