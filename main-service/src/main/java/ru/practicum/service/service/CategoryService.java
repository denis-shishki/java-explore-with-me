package ru.practicum.service.service;

import ru.practicum.service.model.Category;
import ru.practicum.service.model.dto.CategoryDto;

public interface CategoryService {
    CategoryDto postCategory(CategoryDto categoryDto);
    void deleteCategory(long id);
    CategoryDto patchCategory(long id, CategoryDto categoryDto);
}
