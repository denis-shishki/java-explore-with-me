package ru.practicum.service.model.mapper;

import ru.practicum.service.model.Category;
import ru.practicum.service.model.dto.CategoryDto;

public class CategoryMapper {
    public static Category toCategory(CategoryDto categoryDto) {
        return new Category(categoryDto.getId(), categoryDto.getName());
    }

    public static CategoryDto toCategoryDto(Category category) {
        return new CategoryDto(category.getId(), category.getName());
    }
}
