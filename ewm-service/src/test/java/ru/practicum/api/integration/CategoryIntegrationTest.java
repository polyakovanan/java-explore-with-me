package ru.practicum.api.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.core.persistance.model.Category;
import ru.practicum.core.persistance.model.Event;
import ru.practicum.core.persistance.model.User;
import ru.practicum.core.persistance.model.dto.category.NewCategoryDto;
import ru.practicum.core.persistance.model.dto.event.state.EventState;
import ru.practicum.core.persistance.repository.CategoryRepository;
import ru.practicum.core.persistance.repository.EventRepository;
import ru.practicum.core.persistance.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class CategoryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    private NewCategoryDto newCategoryDto;
    private Category existingCategory;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();
        eventRepository.deleteAll();
        userRepository.deleteAll();

        newCategoryDto = new NewCategoryDto();
        newCategoryDto.setName("Концерты");

        existingCategory = new Category();
        existingCategory.setName("Кино");
        categoryRepository.save(existingCategory);
    }

    @Test
    void createCategoryShouldSaveToDatabase() throws Exception {
        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCategoryDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name").value(newCategoryDto.getName()));

        List<Category> categories = categoryRepository.findAll();
        assertEquals(2, categories.size());
        assertTrue(categories.stream().anyMatch(c -> c.getName().equals(newCategoryDto.getName())));
    }

    @Test
    void createCategoryWithDuplicateNameShouldReturnConflict() throws Exception {
        NewCategoryDto duplicateDto = new NewCategoryDto();
        duplicateDto.setName(existingCategory.getName());

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateDto)))
                .andExpect(status().isConflict());
    }

    @Test
    void updateCategoryShouldModifyInDatabase() throws Exception {
        NewCategoryDto updateDto = new NewCategoryDto();
        updateDto.setName("Обновленное кино");

        mockMvc.perform(patch("/admin/categories/{id}", existingCategory.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingCategory.getId()))
                .andExpect(jsonPath("$.name").value(updateDto.getName()));

        Category updatedCategory = categoryRepository.findById(existingCategory.getId()).orElseThrow();
        assertEquals(updateDto.getName(), updatedCategory.getName());
    }

    @Test
    void deleteCategoryShouldRemoveFromDatabase() throws Exception {
        mockMvc.perform(delete("/admin/categories/{id}", existingCategory.getId()))
                .andExpect(status().isNoContent());

        assertTrue(categoryRepository.findById(existingCategory.getId()).isEmpty());
    }

    @Test
    void getAllCategoriesShouldReturnAllCategories() throws Exception {
        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(existingCategory.getId()))
                .andExpect(jsonPath("$[0].name").value(existingCategory.getName()));
    }

    @Test
    void getCategoryByIdShouldReturnCorrectCategory() throws Exception {
        mockMvc.perform(get("/categories/{id}", existingCategory.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingCategory.getId()))
                .andExpect(jsonPath("$.name").value(existingCategory.getName()));
    }

    @Test
    void getNonExistentCategoryShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/categories/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCategoryWithEvents_shouldReturnConflict() throws Exception {
        User initiator = new User();
        initiator.setName("Инициатор");
        initiator.setEmail("initiator@example.com");
        User savedInitiator = userRepository.save(initiator);

        Event event = new Event();
        event.setAnnotation("Тестовое описание события");
        event.setCategory(existingCategory);
        event.setDescription("Полное описание тестового события");
        event.setEventDate(LocalDateTime.now().plusDays(1));
        event.setInitiator(savedInitiator);
        event.setLat(55.754167);
        event.setLon(37.620000);
        event.setPaid(false);
        event.setParticipantLimit(10L);
        event.setRequestModeration(true);
        event.setState(EventState.PENDING);
        event.setTitle("Тестовое событие");
        event.setCreatedOn(LocalDateTime.now());
        eventRepository.save(event);

        mockMvc.perform(delete("/admin/categories/{id}", existingCategory.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "Удаление категории невозможно, так как она используется в событиях"
                ));

        assertTrue(categoryRepository.findById(existingCategory.getId()).isPresent());
    }
}