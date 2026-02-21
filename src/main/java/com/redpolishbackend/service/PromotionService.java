package com.redpolishbackend.service;

import com.redpolishbackend.dto.PromotionDto;
import java.util.List;

public interface PromotionService {
    PromotionDto create(PromotionDto dto);
    List<PromotionDto> getAll();
    PromotionDto update(Long id, PromotionDto dto);
    void delete(Long id);
    PromotionDto getById(Long id);
    void sendEmailActivePromotions(Long promotionId);
}
