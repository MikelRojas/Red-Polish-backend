package com.redpolishbackend.service;

import com.redpolishbackend.dto.CategoryDto;
import com.redpolishbackend.dto.ProductDto;
import com.redpolishbackend.dto.UserDto;

import java.util.List;

public interface CategoryService {
    CategoryDto createCategory(CategoryDto category);
    List<CategoryDto> getCategories();
}
