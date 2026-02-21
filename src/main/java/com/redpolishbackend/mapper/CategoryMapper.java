package com.redpolishbackend.mapper;

import com.redpolishbackend.dto.CategoryDto;
import com.redpolishbackend.entity.Category;

public class CategoryMapper {

    public static CategoryDto mapToCategoryDto(Category category) {
        return new CategoryDto(
                category.getId(),
                category.getName()
        );
    }

    public static Category mapToCategory(CategoryDto dto) {
        return new Category(
                dto.getId(),
                dto.getName()
        );
    }
}