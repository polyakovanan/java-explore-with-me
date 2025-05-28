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
import ru.practicum.core.persistance.model.dto.event.EventFullDto;
import ru.practicum.core.persistance.model.dto.event.UpdateEventAdminRequest;
import ru.practicum.core.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventService eventService;

    private EventFullDto eventFullDto;
    private UpdateEventAdminRequest updateRequest;

    @BeforeEach
    void setUp() {
        eventFullDto = new EventFullDto();
        eventFullDto.setId(1L);
        eventFullDto.setTitle("Test Event");
        eventFullDto.setAnnotation("Test Annotation");
        eventFullDto.setEventDate(LocalDateTime.now().plusDays(1));

        updateRequest = new UpdateEventAdminRequest();
        updateRequest.setTitle("Updated Title");
    }

    @Test
    void getAllEventsWithAllParamsShouldReturnOk() throws Exception {
        Mockito.when(eventService.searchAdmin(any()))
                .thenReturn(List.of(eventFullDto));

        mockMvc.perform(get("/admin/events")
                        .param("users", "1,2")
                        .param("states", "PENDING,PUBLISHED")
                        .param("categories", "1,2")
                        .param("rangeStart", "2023-01-01 00:00:00")
                        .param("rangeEnd", "2023-12-31 23:59:59")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(eventFullDto.getId()))
                .andExpect(jsonPath("$[0].title").value(eventFullDto.getTitle()));

        Mockito.verify(eventService).searchAdmin(any());
    }

    @Test
    void getAllEventsWithMinimalParamsShouldReturnOk() throws Exception {
        Mockito.when(eventService.searchAdmin(any()))
                .thenReturn(List.of(eventFullDto));

        mockMvc.perform(get("/admin/events")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        Mockito.verify(eventService).searchAdmin(any());
    }

    @Test
    void getAllEventsWithInvalidFromParamShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/admin/events")
                        .param("from", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllEventsWithInvalidSizeParamShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/admin/events")
                        .param("from", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateEventWhenValidShouldReturnOk() throws Exception {
        Mockito.when(eventService.updateByAdmin(anyLong(), any()))
                .thenReturn(eventFullDto);

        mockMvc.perform(patch("/admin/events/{eventId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventFullDto.getId()))
                .andExpect(jsonPath("$.title").value(eventFullDto.getTitle()));

        Mockito.verify(eventService).updateByAdmin(anyLong(), any());
    }

    @Test
    void updateEventWhenInvalidBodyShouldReturnBadRequest() throws Exception {
        UpdateEventAdminRequest invalidRequest = new UpdateEventAdminRequest();
        invalidRequest.setTitle(""); // Пустой заголовок - невалидный

        mockMvc.perform(patch("/admin/events/{eventId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}