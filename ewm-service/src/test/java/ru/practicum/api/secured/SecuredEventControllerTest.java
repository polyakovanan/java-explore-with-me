package ru.practicum.api.secured;

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
import ru.practicum.core.persistance.model.dto.event.*;
import ru.practicum.core.persistance.model.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.core.persistance.model.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.core.persistance.model.dto.request.ParticipationRequestDto;
import ru.practicum.core.persistance.model.dto.request.ParticipationRequestStatus;
import ru.practicum.core.service.EventService;
import ru.practicum.core.service.ParticipationRequestService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecuredEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventService eventService;

    @MockBean
    private ParticipationRequestService participationRequestService;

    private EventShortDto eventShortDto;
    private EventFullDto eventFullDto;
    private NewEventDto newEventDto;
    private UpdateEventUserRequest updateEventUserRequest;
    private ParticipationRequestDto participationRequestDto;
    private EventRequestStatusUpdateRequest statusUpdateRequest;
    private EventRequestStatusUpdateResult statusUpdateResult;

    @BeforeEach
    void setUp() {
        eventShortDto = new EventShortDto();
        eventShortDto.setId(1L);
        eventShortDto.setTitle("Test Event Short");

        eventFullDto = new EventFullDto();
        eventFullDto.setId(1L);
        eventFullDto.setTitle("Test Event Full");

        newEventDto = NewEventDto.builder()
                .annotation("Очень подробная аннотация предстоящего события, которая содержит все необходимые детали")
                .category(1L)
                .description("Полное описание события со всеми деталями и важной информацией для участников. " +
                        "Должно быть достаточно длинным, чтобы соответствовать требованиям валидации.")
                .eventDate(LocalDateTime.now().plusDays(2))
                .location(new Location(55.754167, 37.620000)) // Москва
                .paid(true)
                .participantLimit(100L)
                .requestModeration(true)
                .title("Интересное событие с длинным названием")
                .build();

        updateEventUserRequest = new UpdateEventUserRequest();
        updateEventUserRequest.setTitle("Updated Title");

        participationRequestDto = new ParticipationRequestDto();
        participationRequestDto.setId(1L);
        participationRequestDto.setEvent(1L);
        participationRequestDto.setRequester(1L);

        statusUpdateRequest = new EventRequestStatusUpdateRequest();
        statusUpdateRequest.setRequestIds(Set.of(1L));
        statusUpdateRequest.setStatus(ParticipationRequestStatus.CONFIRMED);

        statusUpdateResult = new EventRequestStatusUpdateResult();
        statusUpdateResult.setConfirmedRequests(Set.of(participationRequestDto));
    }

    @Test
    void getEventsByUserIdShouldReturnEventShortDtoList() throws Exception {
        Mockito.when(eventService.findByUserId(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(eventShortDto));

        mockMvc.perform(get("/users/{userId}/events", 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(eventShortDto.getId()))
                .andExpect(jsonPath("$[0].title").value(eventShortDto.getTitle()));

        Mockito.verify(eventService).findByUserId(1L, 0, 10);
    }

    @Test
    void getEventByUserIdShouldReturnEventFullDto() throws Exception {
        Mockito.when(eventService.findByIdAndUser(anyLong(), anyLong()))
                .thenReturn(eventFullDto);

        mockMvc.perform(get("/users/{userId}/events/{eventId}", 1L, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventFullDto.getId()))
                .andExpect(jsonPath("$.title").value(eventFullDto.getTitle()));

        Mockito.verify(eventService).findByIdAndUser(1L, 1L);
    }

    @Test
    void getRequestsShouldReturnParticipationRequestList() throws Exception {
        Mockito.when(participationRequestService.getAllByEventAndInitiator(anyLong(), anyLong()))
                .thenReturn(List.of(participationRequestDto));

        mockMvc.perform(get("/users/{userId}/events/{eventId}/requests", 1L, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(participationRequestDto.getId()));

        Mockito.verify(participationRequestService).getAllByEventAndInitiator(1L, 1L);
    }

    @Test
    void createShouldReturnCreatedEvent() throws Exception {
        Mockito.when(eventService.create(anyLong(), any(NewEventDto.class)))
                .thenReturn(eventFullDto);

        mockMvc.perform(post("/users/{userId}/events", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(eventFullDto.getId()))
                .andExpect(jsonPath("$.title").value(eventFullDto.getTitle()));

        Mockito.verify(eventService).create(eq(1L), any(NewEventDto.class));
    }

    @Test
    void createWithInvalidDataShouldReturnBadRequest() throws Exception {
        NewEventDto invalidDto = new NewEventDto();

        mockMvc.perform(post("/users/{userId}/events", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateShouldReturnUpdatedEvent() throws Exception {
        Mockito.when(eventService.updateByUser(anyLong(), anyLong(), any(UpdateEventUserRequest.class)))
                .thenReturn(eventFullDto);

        mockMvc.perform(patch("/users/{userId}/events/{eventId}", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEventUserRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventFullDto.getId()))
                .andExpect(jsonPath("$.title").value(eventFullDto.getTitle()));

        Mockito.verify(eventService).updateByUser(eq(1L), eq(1L), any(UpdateEventUserRequest.class));
    }

    @Test
    void updateWithInvalidDataShouldReturnBadRequest() throws Exception {
        UpdateEventUserRequest invalidRequest = new UpdateEventUserRequest();
        invalidRequest.setTitle("");

        mockMvc.perform(patch("/users/{userId}/events/{eventId}", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateRequestShouldReturnStatusUpdateResult() throws Exception {
        Mockito.when(participationRequestService.updateStatus(anyLong(), anyLong(), any(EventRequestStatusUpdateRequest.class)))
                .thenReturn(statusUpdateResult);

        mockMvc.perform(patch("/users/{userId}/events/{eventId}/requests", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmedRequests[0].id").value(participationRequestDto.getId()));

        Mockito.verify(participationRequestService).updateStatus(eq(1L), eq(1L), any(EventRequestStatusUpdateRequest.class));
    }
}