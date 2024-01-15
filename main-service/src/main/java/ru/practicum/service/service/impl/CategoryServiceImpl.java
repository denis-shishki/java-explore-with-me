package ru.practicum.service.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.service.exception.NotFoundException;
import ru.practicum.service.model.Category;
import ru.practicum.service.model.dto.CategoryDto;
import ru.practicum.service.model.mapper.CategoryMapper;
import ru.practicum.service.paginator.Paginator;
import ru.practicum.service.repository.CategoryRepository;
import ru.practicum.service.service.CategoryService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    @Override
    public CategoryDto postCategory(CategoryDto categoryDto) {
        Category category = CategoryMapper.toCategory(categoryDto);
        category = categoryRepository.save(category);
        return CategoryMapper.toCategoryDto(category);
    }

    @Override
    public void deleteCategory(long id) {
        try {
            categoryRepository.deleteById(id);
            //todo проверка на использование этой категории, добавить исключение "категория занята" 409
//            if() {
//                throw
//            }
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Category with id=" + id + " was not found");
        } /*catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException("The category is not empty");
        }*/
    }

    @Override
    public CategoryDto patchCategory(long id, CategoryDto categoryDto) {
        Category categoryUpdate = CategoryMapper.toCategory(categoryDto);
        Optional<Category> categoryOldOpt = categoryRepository.findById(id);

        if (categoryOldOpt.isEmpty()) {
            throw new NotFoundException("Category with id=" + id + "was not found");
        }

        Category categoryOld = categoryOldOpt.get();
        categoryUpdate.setId(categoryOld.getId());

        return CategoryMapper.toCategoryDto(categoryRepository.save(categoryUpdate));
    }

    @Override
    public List<CategoryDto> getCategories(int from, int size) {
        Pageable pageable = Paginator.getPageable(from, size);
        Page<Category> categories = categoryRepository.findAll(pageable);

        return categories.stream().map(CategoryMapper::toCategoryDto).collect(Collectors.toList());
    }

    @Override
    public CategoryDto getCategoryById(long catId) {
        Optional<Category> category = categoryRepository.findById(catId);

        if (category.isEmpty()) {
            throw new NotFoundException("Category with id=" + catId + " was not found");
        }

        return CategoryMapper.toCategoryDto(category.get());
    }

    @Override
    public Category checkExistCategory(long categoryId) {
        Optional<Category> category = categoryRepository.findById(categoryId);

        if (category.isEmpty()) {
            throw new NotFoundException("Category with id=" + categoryId + " was not found");
        } else {
            return category.get();
        }
    }
}
