package ru.practicum.core.persistance.model.mapper;

import ru.practicum.core.persistance.model.Category;
import ru.practicum.core.persistance.model.dto.category.CategoryDto;
import ru.practicum.core.persistance.model.dto.category.NewCategoryDto;

public class CategoryMapper {

    private CategoryMapper() {

    }

    public static Category requestToCategory(NewCategoryDto categoryRequest) {
        return Category.builder()
                .name(categoryRequest.getName())
                .build();
    }

    public static CategoryDto categoryToDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    public static Category dtoToCategory(CategoryDto dto) {
        return Category.builder()
                .id(dto.getId())
                .name(dto.getName())
                .build();
    }
}
