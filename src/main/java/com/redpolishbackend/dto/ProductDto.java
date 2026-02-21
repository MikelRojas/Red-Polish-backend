package com.redpolishbackend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProductDto {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private Integer stock;
    private String image;
    private Long promotionId;
    private Long categoryId;

    public ProductDto(ProductDto other) {
        this.id = other.id;
        this.name = other.name;
        this.description = other.description;
        this.price = other.price;
        this.stock = other.stock;
        this.image = other.image;
        this.promotionId = other.promotionId;
        this.categoryId = other.categoryId;
    }
}