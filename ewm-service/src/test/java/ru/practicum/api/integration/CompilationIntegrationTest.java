package ru.practicum.api.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.core.persistance.model.Category;
import ru.practicum.core.persistance.model.Compilation;
import ru.practicum.core.persistance.model.Event;
import ru.practicum.core.persistance.model.User;
import ru.practicum.core.persistance.model.dto.compilation.NewCompilationDto;
import ru.practicum.core.persistance.model.dto.compilation.UpdateCompilationRequest;
import ru.practicum.core.persistance.model.dto.event.state.EventState;
import ru.practicum.core.persistance.repository.CategoryRepository;
import ru.practicum.core.persistance.repository.CompilationRepository;
import ru.practicum.core.persistance.repository.EventRepository;
import ru.practicum.core.persistance.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class CompilationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CompilationRepository compilationRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    private Event testEvent;

    @BeforeEach
    void setUp() {
        compilationRepository.deleteAll();
        eventRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        Category category = categoryRepository.save(new Category(null, "Test Category"));
        User user = userRepository.save(new User(null, "Test User", "test@email.com"));
        testEvent = eventRepository.save(Event.builder()
                .title("Test Event")
                .annotation("Test Annotation")
                .description("Test Description")
                .eventDate(LocalDateTime.now().plusDays(1))
                .initiator(user)
                .category(category)
                .paid(false)
                .participantLimit(10L)
                .requestModeration(true)
                .state(EventState.PENDING)
                .createdOn(LocalDateTime.now())
                .lat(55.754167)
                .lon(37.620000)
                .build());
    }

    @Test
    void shouldCreateGetAndDeleteCompilation() throws Exception {
        NewCompilationDto newCompilation = new NewCompilationDto();
        newCompilation.setTitle("New Compilation");
        newCompilation.setPinned(true);
        newCompilation.setEvents(Set.of(testEvent.getId()));

        mockMvc.perform(post("/admin/compilations")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(newCompilation)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.title", is("New Compilation")))
                .andExpect(jsonPath("$.pinned", is(true)))
                .andExpect(jsonPath("$.events[0].id", is(testEvent.getId().intValue())));

        List<Compilation> compilations = compilationRepository.findAll();
        assertEquals(1, compilations.size());
        assertEquals("New Compilation", compilations.getFirst().getTitle());

        Long compilationId = compilations.getFirst().getId();

        mockMvc.perform(get("/compilations/{compId}", compilationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(compilationId.intValue())))
                .andExpect(jsonPath("$.title", is("New Compilation")));

        mockMvc.perform(get("/compilations")
                        .param("pinned", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(compilationId.intValue())));

        UpdateCompilationRequest updateRequest = new UpdateCompilationRequest();
        updateRequest.setTitle("Updated Compilation");
        updateRequest.setPinned(false);

        mockMvc.perform(patch("/admin/compilations/{compId}", compilationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Updated Compilation")))
                .andExpect(jsonPath("$.pinned", is(false)));

        mockMvc.perform(delete("/admin/compilations/{compId}", compilationId))
                .andExpect(status().isNoContent());

        assertEquals(0, compilationRepository.count());
    }

    @Test
    void shouldNotCreateCompilationWithDuplicateTitle() throws Exception {
        NewCompilationDto firstCompilation = new NewCompilationDto();
        firstCompilation.setTitle("Duplicate Title");
        firstCompilation.setPinned(false);

        mockMvc.perform(post("/admin/compilations")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(firstCompilation)))
                .andExpect(status().isCreated());

        NewCompilationDto duplicateCompilation = new NewCompilationDto();
        duplicateCompilation.setTitle("Duplicate Title");
        duplicateCompilation.setPinned(true);

        mockMvc.perform(post("/admin/compilations")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(duplicateCompilation)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturnNotFoundForNonExistingCompilation() throws Exception {
        mockMvc.perform(get("/compilations/999"))
                .andExpect(status().isNotFound());

        mockMvc.perform(patch("/admin/compilations/999")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/admin/compilations/999"))
                .andExpect(status().isNotFound());
    }
}