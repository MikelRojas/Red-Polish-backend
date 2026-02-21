package com.redpolishbackend.mapper;

import com.redpolishbackend.dto.PromotionDto;
import com.redpolishbackend.entity.Promotion;

public class PromotionMapper {

    public static PromotionDto mapToPromotionDto(Promotion promotion) {
        return new PromotionDto(
                promotion.getId(),
                promotion.getTitle(),
                promotion.getDescription(),
                promotion.getPorcentage(),
                promotion.getStart_date(),
                promotion.getEnd_date()
        );
    }

    public static Promotion mapToPromotion(PromotionDto dto) {
        return new Promotion(
                dto.getId(),
                dto.getTitle(),
                dto.getDescription(),
                dto.getPorcentage(),
                dto.getStart_date(),
                dto.getEnd_date()
        );
    }
}
