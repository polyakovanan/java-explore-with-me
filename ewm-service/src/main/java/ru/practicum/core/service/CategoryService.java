package ru.practicum.core.service;

import ru.practicum.core.persistance.model.dto.category.CategoryDto;
import ru.practicum.core.persistance.model.dto.category.NewCategoryDto;

import java.util.List;

public interface CategoryService {
    List<CategoryDto> getAll(Integer from, Integer size);

    CategoryDto getById(Long id);

    CategoryDto create(NewCategoryDto newCategoryDto);

    CategoryDto update(Long id, NewCategoryDto newCategoryDto);

    void delete(Long id);
}
