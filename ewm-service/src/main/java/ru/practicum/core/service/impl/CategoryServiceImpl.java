package ru.practicum.core.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.core.exception.ConditionsNotMetException;
import ru.practicum.core.exception.NotFoundException;
import ru.practicum.core.persistance.model.Category;
import ru.practicum.core.persistance.model.dto.category.CategoryDto;
import ru.practicum.core.persistance.model.dto.category.NewCategoryDto;
import ru.practicum.core.persistance.model.mapper.CategoryMapper;
import ru.practicum.core.persistance.repository.CategoryRepository;
import ru.practicum.core.persistance.repository.EventRepository;
import ru.practicum.core.service.CategoryService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    public List<CategoryDto> getAll(Integer from, Integer size) {
        return categoryRepository.findCategories(from, size).stream().map(CategoryMapper::categoryToDto).toList();
    }

    @Override
    public CategoryDto getById(Long id) {
        return CategoryMapper.categoryToDto(
                categoryRepository.findById(id).orElseThrow(
                        () -> new NotFoundException("Категория с id=" + id + " не найдена")
                )
        );
    }

    @Override
    public CategoryDto create(NewCategoryDto newCategoryDto) {
        if (!categoryRepository.findByNameIgnoreCase(newCategoryDto.getName()).isEmpty()) {
            throw new ConditionsNotMetException("Категория с именем " + newCategoryDto.getName() + " уже существует");
        }

        return CategoryMapper.categoryToDto(
                categoryRepository.saveAndFlush(CategoryMapper.requestToCategory(newCategoryDto))
        );
    }

    @Override
    public CategoryDto update(Long id, NewCategoryDto newCategoryDto) {
        Category category = categoryRepository.findById(id).orElseThrow(() ->
                new NotFoundException("Категория с id=" + id + " не найдена")
        );
        List<Category> categories = categoryRepository.findByNameIgnoreCase(newCategoryDto.getName());
        if (!categories.isEmpty() && !categories.getFirst().getId().equals(id)) {
            throw new ConditionsNotMetException("Категория с именем " + newCategoryDto.getName() + " уже существует");
        }

        category.setName(newCategoryDto.getName());
        return CategoryMapper.categoryToDto(categoryRepository.saveAndFlush(category));
    }

    @Override
    public void delete(Long id) {
        categoryRepository.findById(id).orElseThrow(() ->
                new NotFoundException("Категория с id=" + id + " не найдена")
        );
        if (!eventRepository.findAllByCategoryId(id).isEmpty()) {
            throw new ConditionsNotMetException("Удаление категории невозможно, так как она используется в событиях");
        }
        categoryRepository.deleteById(id);
    }
}
