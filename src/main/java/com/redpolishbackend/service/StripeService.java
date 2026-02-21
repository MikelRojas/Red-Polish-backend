package com.redpolishbackend.service;

import com.redpolishbackend.dto.CompraDto;
import com.redpolishbackend.dto.PayDto;
import com.redpolishbackend.entity.Compra;
import com.redpolishbackend.entity.ShoppingCart;
import com.redpolishbackend.entity.User;
import com.redpolishbackend.exception.ResourceNotFoundException;
import com.redpolishbackend.repository.CompraRepository;
import com.redpolishbackend.repository.ShoppingCartRepository;
import com.redpolishbackend.repository.UserRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.redpolishbackend.mapper.CompraMapper.mapToCompra;

@Service
@RequiredArgsConstructor
public class StripeService {

    private final UserRepository userRepository;
    private final ShoppingCartRepository shoppingCartRepository;
    private final CompraRepository compraRepository;

    @Value("${stripe.secretKey}")
    private String secretKey;

    public PayDto checkoutProducts(CompraDto compraDto) {
        User existingUser = userRepository.findByEmail(compraDto.getUsuarioEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con email " + compraDto.getUsuarioEmail() + " no encontrado"));

        List<ShoppingCart> productsList = shoppingCartRepository.findByUser(existingUser);

        if (productsList.isEmpty()) {
            throw new IllegalStateException("El carrito de compras está vacío");
        }

        double totalPrice = 0;
        for (ShoppingCart cart : productsList) {
            totalPrice += (cart.getProduct().getPrice() * cart.getQuantity());
        }

        Compra compra = mapToCompra(compraDto, existingUser, totalPrice);
        Compra savedCompra = compraRepository.save(compra);

        Stripe.apiKey = secretKey;
        SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://red-polish-app.s3-website.us-east-2.amazonaws.com/shopping-cart")
                .setCancelUrl("http://red-polish-app.s3-website.us-east-2.amazonaws.com/cancel");


        for (ShoppingCart cart : productsList) {
            SessionCreateParams.LineItem.PriceData.ProductData productData =
                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                            .setName(cart.getProduct().getName())
                            .build();

            SessionCreateParams.LineItem.PriceData priceData =
                    SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency("USD")
                            .setUnitAmount((long) (cart.getProduct().getPrice() * 100)) // Stripe usa centavos
                            .setProductData(productData)
                            .build();

            SessionCreateParams.LineItem lineItem =
                    SessionCreateParams.LineItem.builder()
                            .setQuantity((long) cart.getQuantity())
                            .setPriceData(priceData)
                            .build();

            paramsBuilder.addLineItem(lineItem);
        }

        Session session;
        try {
            session = Session.create(paramsBuilder.build());
        } catch (StripeException e) {
            throw new RuntimeException("Error al crear la sesión de Stripe: " + e.getMessage());
        }

        return PayDto.builder()
                .status("Success")
                .message("Payment session created")
                .sessionId(session.getId())
                .sessionUrl(session.getUrl())
                .id_compra(savedCompra.getIdCompra())
                .build();
    }

}
