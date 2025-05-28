package ru.practicum.api.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.core.persistance.model.dto.compilation.CompilationDto;
import ru.practicum.core.persistance.model.dto.compilation.NewCompilationDto;
import ru.practicum.core.persistance.model.dto.compilation.UpdateCompilationRequest;
import ru.practicum.core.service.CompilationService;

import java.util.Collections;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminCompilationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CompilationService compilationService;

    @Test
    void createShouldReturnCreatedCompilation() throws Exception {
        NewCompilationDto newCompilationDto = new NewCompilationDto();
        newCompilationDto.setTitle("New Compilation");
        newCompilationDto.setPinned(true);
        newCompilationDto.setEvents(Set.of(1L));

        CompilationDto expectedDto = new CompilationDto();
        expectedDto.setId(1L);
        expectedDto.setTitle("New Compilation");
        expectedDto.setPinned(true);
        expectedDto.setEvents(Collections.emptyList());

        Mockito.when(compilationService.create(any(NewCompilationDto.class)))
                .thenReturn(expectedDto);

        mockMvc.perform(post("/admin/compilations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCompilationDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("New Compilation"))
                .andExpect(jsonPath("$.pinned").value(true));
    }

    @Test
    void createShouldReturnBadRequestWhenTitleIsBlank() throws Exception {
        NewCompilationDto invalidDto = new NewCompilationDto();
        invalidDto.setTitle(" ");
        invalidDto.setPinned(false);

        mockMvc.perform(post("/admin/compilations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteShouldReturnNoContent() throws Exception {
        Mockito.doNothing().when(compilationService).delete(anyLong());

        mockMvc.perform(delete("/admin/compilations/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateShouldReturnUpdatedCompilation() throws Exception {
        UpdateCompilationRequest updateRequest = new UpdateCompilationRequest();
        updateRequest.setTitle("Updated Title");
        updateRequest.setPinned(true);
        updateRequest.setEvents(Set.of(1L));

        CompilationDto expectedDto = new CompilationDto();
        expectedDto.setId(1L);
        expectedDto.setTitle("Updated Title");
        expectedDto.setPinned(true);
        expectedDto.setEvents(Collections.emptyList());

        Mockito.when(compilationService.update(anyLong(), any(UpdateCompilationRequest.class)))
                .thenReturn(expectedDto);

        mockMvc.perform(patch("/admin/compilations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.pinned").value(true));
    }

    @Test
    void updateShouldReturnBadRequestWhenEmptyBody() throws Exception {
        mockMvc.perform(patch("/admin/compilations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }
}