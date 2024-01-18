package ru.practicum.ewm.model.mapper;

import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.model.dto.CategoryDto;

public class CategoryMapper {
    public static Category toCategory(CategoryDto categoryDto) {
        return new Category(categoryDto.getId(), categoryDto.getName());
    }

    public static CategoryDto toCategoryDto(Category category) {
        return new CategoryDto(category.getId(), category.getName());
    }
}
