package ru.practicum.core.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.core.exception.ConditionsNotMetException;
import ru.practicum.core.exception.NotFoundException;
import ru.practicum.core.persistance.model.Category;
import ru.practicum.core.persistance.model.Event;
import ru.practicum.core.persistance.model.dto.category.CategoryDto;
import ru.practicum.core.persistance.model.dto.category.NewCategoryDto;
import ru.practicum.core.persistance.repository.CategoryRepository;
import ru.practicum.core.persistance.repository.EventRepository;
import ru.practicum.core.service.impl.CategoryServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category;
    private NewCategoryDto newCategoryDto;
    private CategoryDto categoryDto;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Концерты");

        newCategoryDto = new NewCategoryDto();
        newCategoryDto.setName("Концерты");

        categoryDto = new CategoryDto();
        categoryDto.setId(1L);
        categoryDto.setName("Концерты");
    }

    @Test
    void getAllShouldReturnListOfCategories() {
        // Arrange
        when(categoryRepository.findCategories(0, 10)).thenReturn(List.of(category));

        // Act
        List<CategoryDto> result = categoryService.getAll(0, 10);

        // Assert
        assertEquals(1, result.size());
        assertEquals(categoryDto.getId(), result.getFirst().getId());
        assertEquals(categoryDto.getName(), result.getFirst().getName());
        verify(categoryRepository).findCategories(0, 10);
    }

    @Test
    void getByIdWhenExistsShouldReturnCategory() {
        // Arrange
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        // Act
        CategoryDto result = categoryService.getById(1L);

        // Assert
        assertEquals(categoryDto.getId(), result.getId());
        assertEquals(categoryDto.getName(), result.getName());
        verify(categoryRepository).findById(1L);
    }

    @Test
    void getByIdWhenNotExistsShouldThrowNotFoundException() {
        // Arrange
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> categoryService.getById(999L)
        );
        assertEquals("Категория с id=999 не найдена", exception.getMessage());
        verify(categoryRepository).findById(999L);
    }

    @Test
    void createWhenValidShouldReturnCreatedCategory() {
        // Arrange
        when(categoryRepository.findByNameIgnoreCase("Концерты")).thenReturn(List.of());
        when(categoryRepository.saveAndFlush(any(Category.class))).thenReturn(category);

        // Act
        CategoryDto result = categoryService.create(newCategoryDto);

        // Assert
        assertEquals(categoryDto.getId(), result.getId());
        assertEquals(categoryDto.getName(), result.getName());
        verify(categoryRepository).findByNameIgnoreCase("Концерты");
        verify(categoryRepository).saveAndFlush(any(Category.class));
    }

    @Test
    void createWhenNameExistsShouldThrowConditionsNotMetException() {
        // Arrange
        when(categoryRepository.findByNameIgnoreCase("Концерты")).thenReturn(List.of(category));

        // Act & Assert
        ConditionsNotMetException exception = assertThrows(
                ConditionsNotMetException.class,
                () -> categoryService.create(newCategoryDto)
        );
        assertEquals("Категория с именем Концерты уже существует", exception.getMessage());
        verify(categoryRepository).findByNameIgnoreCase("Концерты");
        verify(categoryRepository, never()).saveAndFlush(any(Category.class));
    }

    @Test
    void updateWhenValidShouldReturnUpdatedCategory() {
        // Arrange
        Category updatedCategory = new Category();
        updatedCategory.setId(1L);
        updatedCategory.setName("Обновленные концерты");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.findByNameIgnoreCase("Обновленные концерты")).thenReturn(List.of());
        when(categoryRepository.saveAndFlush(any(Category.class))).thenReturn(updatedCategory);

        NewCategoryDto updateDto = new NewCategoryDto();
        updateDto.setName("Обновленные концерты");

        // Act
        CategoryDto result = categoryService.update(1L, updateDto);

        // Assert
        assertEquals(1L, result.getId());
        assertEquals("Обновленные концерты", result.getName());
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).findByNameIgnoreCase("Обновленные концерты");
        verify(categoryRepository).saveAndFlush(any(Category.class));
    }

    @Test
    void updateWhenCategoryNotExistsShouldThrowNotFoundException() {
        // Arrange
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> categoryService.update(999L, newCategoryDto)
        );
        assertEquals("Категория с id=999 не найдена", exception.getMessage());
        verify(categoryRepository).findById(999L);
        verify(categoryRepository, never()).saveAndFlush(any(Category.class));
    }

    @Test
    void updateWhenNameExistsForOtherCategoryShouldThrowConditionsNotMetException() {
        // Arrange
        Category otherCategory = new Category();
        otherCategory.setId(2L);
        otherCategory.setName("Концерты");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.findByNameIgnoreCase("Концерты")).thenReturn(List.of(otherCategory));

        // Act & Assert
        ConditionsNotMetException exception = assertThrows(
                ConditionsNotMetException.class,
                () -> categoryService.update(1L, newCategoryDto)
        );
        assertEquals("Категория с именем Концерты уже существует", exception.getMessage());
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).findByNameIgnoreCase("Концерты");
        verify(categoryRepository, never()).saveAndFlush(any(Category.class));
    }

    @Test
    void deleteWhenValidShouldDeleteCategory() {
        // Arrange
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(eventRepository.findAllByCategoryId(1L)).thenReturn(List.of());

        // Act
        categoryService.delete(1L);

        // Assert
        verify(categoryRepository).findById(1L);
        verify(eventRepository).findAllByCategoryId(1L);
        verify(categoryRepository).deleteById(1L);
    }

    @Test
    void deleteWhenCategoryNotExistsShouldThrowNotFoundException() {
        // Arrange
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> categoryService.delete(999L)
        );
        assertEquals("Категория с id=999 не найдена", exception.getMessage());
        verify(categoryRepository).findById(999L);
        verify(categoryRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteWhenCategoryHasEventsShouldThrowConditionsNotMetException() {
        // Arrange
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(eventRepository.findAllByCategoryId(1L)).thenReturn(List.of(new Event()));

        // Act & Assert
        ConditionsNotMetException exception = assertThrows(
                ConditionsNotMetException.class,
                () -> categoryService.delete(1L)
        );
        assertEquals("Удаление категории невозможно, так как она используется в событиях", exception.getMessage());
        verify(categoryRepository).findById(1L);
        verify(eventRepository).findAllByCategoryId(1L);
        verify(categoryRepository, never()).deleteById(anyLong());
    }


}