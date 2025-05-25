package ru.practicum.api.secured;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.core.persistance.model.dto.request.ParticipationRequestDto;
import ru.practicum.core.persistance.model.dto.request.ParticipationRequestStatus;
import ru.practicum.core.service.ParticipationRequestService;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecuredParticipationRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ParticipationRequestService participationRequestService;

    private ParticipationRequestDto requestDto;

    @BeforeEach
    void setUp() {
        requestDto = new ParticipationRequestDto();
        requestDto.setId(1L);
        requestDto.setEvent(1L);
        requestDto.setRequester(1L);
        requestDto.setStatus(ParticipationRequestStatus.PENDING);
    }

    @Test
    void getAllRequestsShouldReturnRequests() throws Exception {
        Mockito.when(participationRequestService.getAllByUser(anyLong()))
                .thenReturn(List.of(requestDto));

        mockMvc.perform(get("/users/{userId}/requests", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(requestDto.getId()))
                .andExpect(jsonPath("$[0].status").value(requestDto.getStatus().toString()));

        Mockito.verify(participationRequestService).getAllByUser(1L);
    }

    @Test
    void createRequestShouldCreateAndReturnRequest() throws Exception {
        Mockito.when(participationRequestService.create(anyLong(), anyLong()))
                .thenReturn(requestDto);

        mockMvc.perform(post("/users/{userId}/requests?eventId={eventId}", 1L, 1L))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(requestDto.getId()))
                .andExpect(jsonPath("$.status").value(requestDto.getStatus().toString()));

        Mockito.verify(participationRequestService).create(1L, 1L);
    }

    @Test
    void createRequestWithoutEventIdShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/users/{userId}/requests", 1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cancelRequestShouldCancelAndReturnRequest() throws Exception {
        requestDto.setStatus(ParticipationRequestStatus.CANCELED);
        Mockito.when(participationRequestService.cancel(anyLong(), anyLong()))
                .thenReturn(requestDto);

        mockMvc.perform(patch("/users/{userId}/requests/{requestId}/cancel", 1L, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));

        Mockito.verify(participationRequestService).cancel(1L, 1L);
    }
}