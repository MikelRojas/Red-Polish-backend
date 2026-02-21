package com.redpolishbackend.mapper;

import com.redpolishbackend.dto.ServiceDTO;
import com.redpolishbackend.entity.Category;
import com.redpolishbackend.entity.Promotion;
import com.redpolishbackend.entity.Services;
import jakarta.persistence.Column;

public class ServicesMapper {
    public static ServiceDTO mapToServiceDTO(Services services) {
        return new ServiceDTO(
                services.getId(),
                services.getName(),
                services.getCategory() != null ? services.getCategory().getId() : null,
                services.getDescription(),
                services.getDuration(),
                services.getPrice(),
                services.getPromotion() != null ? services.getPromotion().getId() : null,
                services.getImageUrl()
        );
    }

    public static Services mapToService(ServiceDTO serviceDTO, Promotion promotion, Category category) {
        return new Services(
                serviceDTO.getId(),
                serviceDTO.getName(),
                serviceDTO.getDescription(),
                serviceDTO.getDuration(),
                serviceDTO.getPrice(),
                serviceDTO.getImageUrl(),
                promotion,
                category
        );
    }
}
