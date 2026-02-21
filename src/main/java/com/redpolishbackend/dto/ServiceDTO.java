package com.redpolishbackend.dto;


import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ServiceDTO {
    private Long id;
    private String name;
    private Long categoryId;
    private String description;
    private String duration;
    private Double price;
    private Long promotionId;
    private String imageUrl;

    public ServiceDTO(ServiceDTO other) {
        this.id = other.id;
        this.name = other.name;
        this.categoryId = other.categoryId;
        this.description = other.description;
        this.duration = other.duration;
        this.price = other.price;
        this.promotionId = other.promotionId;
        this.imageUrl = other.imageUrl;
    }
}