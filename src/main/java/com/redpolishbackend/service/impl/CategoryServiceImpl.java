package com.redpolishbackend.service.impl;

import com.redpolishbackend.dto.CategoryDto;
import com.redpolishbackend.entity.Category;
import com.redpolishbackend.mapper.CategoryMapper;
import com.redpolishbackend.repository.CategoryRepository;
import com.redpolishbackend.service.CategoryService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private CategoryRepository categoryRepository;

    public CategoryDto createCategory(CategoryDto categoryDto) {
        Category category = CategoryMapper.mapToCategory(categoryDto);
        Category savedCategory = categoryRepository.save(category);
        return CategoryMapper.mapToCategoryDto(savedCategory);
    }

    @Override
    public List<CategoryDto> getCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(CategoryMapper::mapToCategoryDto)
                .toList();
    }
}
