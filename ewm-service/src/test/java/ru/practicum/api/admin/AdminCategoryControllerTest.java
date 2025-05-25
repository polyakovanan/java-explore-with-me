package ru.practicum.api.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.core.exception.NotFoundException;
import ru.practicum.core.persistance.model.dto.category.CategoryDto;
import ru.practicum.core.persistance.model.dto.category.NewCategoryDto;
import ru.practicum.core.service.CategoryService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    private NewCategoryDto newCategoryDto;
    private CategoryDto categoryDto;

    @BeforeEach
    void setUp() {
        newCategoryDto = new NewCategoryDto();
        newCategoryDto.setName("Концерты");

        categoryDto = new CategoryDto();
        categoryDto.setId(1L);
        categoryDto.setName("Концерты");
    }

    @Test
    void createCategoryShouldReturnCreated() throws Exception {
        Mockito.when(categoryService.create(any(NewCategoryDto.class))).thenReturn(categoryDto);

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCategoryDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(categoryDto.getId()))
                .andExpect(jsonPath("$.name").value(categoryDto.getName()));

        verify(categoryService).create(any(NewCategoryDto.class));
    }

    @Test
    void createCategoryWithInvalidDataShouldReturnBadRequest() throws Exception {
        NewCategoryDto invalidDto = new NewCategoryDto(); // name is required

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).create(any(NewCategoryDto.class));
    }

    @Test
    void updateCategoryShouldReturnUpdatedCategory() throws Exception {
        Mockito.when(categoryService.update(anyLong(), any(NewCategoryDto.class))).thenReturn(categoryDto);

        mockMvc.perform(patch("/admin/categories/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCategoryDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryDto.getId()))
                .andExpect(jsonPath("$.name").value(categoryDto.getName()));

        verify(categoryService).update(1L, newCategoryDto);
    }

    @Test
    void updateCategoryWithInvalidDataShouldReturnBadRequest() throws Exception {
        NewCategoryDto invalidDto = new NewCategoryDto(); // name is required

        mockMvc.perform(patch("/admin/categories/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).update(anyLong(), any(NewCategoryDto.class));
    }

    @Test
    void deleteCategoryShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/admin/categories/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(categoryService).delete(1L);
    }

    @Test
    void deleteCategoryWithInvalidIdShouldReturnBadRequest() throws Exception {
        Long nonExistentCategoryId = 999L;
        doThrow(new NotFoundException("Category not found"))
                .when(categoryService).delete(nonExistentCategoryId);

        mockMvc.perform(delete("/admin/categories/{id}", nonExistentCategoryId))
                .andExpect(status().isNotFound());

        Mockito.verify(categoryService).delete(nonExistentCategoryId);
    }
}