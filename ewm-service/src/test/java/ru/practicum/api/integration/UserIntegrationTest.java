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
import ru.practicum.core.persistance.model.User;
import ru.practicum.core.persistance.model.dto.user.NewUserRequest;
import ru.practicum.core.persistance.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private NewUserRequest newUserRequest;

    @BeforeEach
    void setUp() {
        newUserRequest = new NewUserRequest();
        newUserRequest.setName("Test User");
        newUserRequest.setEmail("test@example.com");
    }

    @Test
    void createUserShouldSaveToDatabase() throws Exception {
        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value(newUserRequest.getName()))
                .andExpect(jsonPath("$.email").value(newUserRequest.getEmail()));

        assertEquals(1, userRepository.count());
        var savedUser = userRepository.findAll().get(0);
        assertEquals(newUserRequest.getName(), savedUser.getName());
        assertEquals(newUserRequest.getEmail(), savedUser.getEmail());
    }

    @Test
    void getAllUsersShouldReturnSavedUsers() throws Exception {
        var savedUser = userRepository.save(createTestUser());

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(savedUser.getId()))
                .andExpect(jsonPath("$[0].name").value(savedUser.getName()))
                .andExpect(jsonPath("$[0].email").value(savedUser.getEmail()));
    }

    @Test
    void deleteUserShouldRemoveFromDatabase() throws Exception {
        var savedUser = userRepository.save(createTestUser());

        mockMvc.perform(delete("/admin/users/{userId}", savedUser.getId()))
                .andExpect(status().isNoContent());

        assertFalse(userRepository.existsById(savedUser.getId()));
    }

    @Test
    void deleteNonExistentUserShouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/admin/users/{userId}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void createUserWithExistingEmailShouldReturnConflict() throws Exception {
        userRepository.save(createTestUser());

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserRequest)))
                .andExpect(status().isConflict());
    }

    private User createTestUser() {
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        return user;
    }
}