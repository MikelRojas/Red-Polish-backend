package com.redpolishbackend.mapper;

import com.redpolishbackend.dto.ProductDto;
import com.redpolishbackend.entity.Category;
import com.redpolishbackend.entity.Product;
import com.redpolishbackend.entity.Promotion;

public class ProductMapper {

    public static ProductDto mapToProductDto(Product product) {
        return new ProductDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getImage(),
                product.getPromotion() != null ? product.getPromotion().getId() : null,
                product.getCategory() != null ? product.getCategory().getId() : null
        );
    }

    public static Product mapToProduct(ProductDto dto, Promotion promotion, Category category) {
        return new Product(
                dto.getId(),
                dto.getName(),
                dto.getDescription(),
                dto.getPrice(),
                dto.getStock(),
                dto.getImage(),
                promotion,
                category
        );
    }
}