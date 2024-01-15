package ru.practicum.service.service;

import ru.practicum.service.model.Category;
import ru.practicum.service.model.dto.CategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto postCategory(CategoryDto categoryDto);
    void deleteCategory(long id);
    CategoryDto patchCategory(long id, CategoryDto categoryDto);
    List<CategoryDto> getCategories(int from, int size);
    CategoryDto getCategoryById(long catId);
    Category checkExistCategory(long categoryId);
}
