package ru.practicum.api.secured;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.core.persistance.model.dto.event.EventShortDto;
import ru.practicum.core.persistance.model.dto.user.UserShortDto;
import ru.practicum.core.service.SubscriptionService;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecuredSubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SubscriptionService subscriptionService;

    private final Long userId = 1L;
    private final Long initiatorId = 2L;
    private final Long subscriberId = 3L;

    @Test
    void getSubscriptionsShouldReturnUserShortDtoList() throws Exception {
        UserShortDto userShortDto = new UserShortDto(initiatorId, "Test User", 0L);
        Mockito.when(subscriptionService.getSubscriptions(anyLong()))
                .thenReturn(List.of(userShortDto));

        mockMvc.perform(get("/users/{userId}/subscriptions", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(initiatorId.intValue())))
                .andExpect(jsonPath("$[0].name", is(userShortDto.getName())));

        Mockito.verify(subscriptionService).getSubscriptions(userId);
    }

    @Test
    void getSubscribersShouldReturnUserShortDtoList() throws Exception {
        UserShortDto userShortDto = new UserShortDto(subscriberId, "Subscriber", 0L);
        Mockito.when(subscriptionService.getSubscribers(anyLong()))
                .thenReturn(List.of(userShortDto));

        mockMvc.perform(get("/users/{userId}/subscriptions/subscribers", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(subscriberId.intValue())))
                .andExpect(jsonPath("$[0].name", is(userShortDto.getName())));

        Mockito.verify(subscriptionService).getSubscribers(userId);
    }

    @Test
    void subscribeShouldReturnCreated() throws Exception {
        mockMvc.perform(post("/users/{userId}/subscriptions/{initiatorId}", userId, initiatorId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        Mockito.verify(subscriptionService).subscribe(userId, initiatorId);
    }

    @Test
    void cancelShouldReturnNoContent() throws Exception {
        mockMvc.perform(patch("/users/{userId}/subscriptions/{initiatorId}/cancel", userId, initiatorId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        Mockito.verify(subscriptionService).unsubscribe(userId, initiatorId);
    }

    @Test
    void removeShouldReturnNoContent() throws Exception {
        mockMvc.perform(patch("/users/{userId}/subscriptions/{subscriberId}/remove", userId, subscriberId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        Mockito.verify(subscriptionService).remove(userId, subscriberId);
    }

    @Test
    void getSubscriptionEventsShouldReturnEventShortDtoList() throws Exception {
        EventShortDto eventShortDto = EventShortDto.builder()
                .id(1L)
                .title("Test Event")
                .build();

        Mockito.when(subscriptionService.getSubscribedEvents(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(eventShortDto));

        mockMvc.perform(get("/users/{userId}/subscriptions/events", userId)
                        .param("from", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(eventShortDto.getId().intValue())))
                .andExpect(jsonPath("$[0].title", is(eventShortDto.getTitle())));

        Mockito.verify(subscriptionService).getSubscribedEvents(userId, 0, 10);
    }

    @Test
    void getSubscriptionEventsShouldUseDefaultPagination() throws Exception {
        Mockito.when(subscriptionService.getSubscribedEvents(anyLong(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/users/{userId}/subscriptions/events", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Mockito.verify(subscriptionService).getSubscribedEvents(userId, 0, 10);
    }

    @Test
    void getSubscriptionEventsByInitiatorShouldReturnEventShortDtoList() throws Exception {
        EventShortDto eventShortDto = EventShortDto.builder()
                .id(1L)
                .title("Test Event")
                .build();

        Mockito.when(subscriptionService.getSubscribedEventsByInitiator(anyLong(), anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(eventShortDto));

        mockMvc.perform(get("/users/{userId}/subscriptions/events/{initiatorId}", userId, initiatorId)
                        .param("from", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(eventShortDto.getId().intValue())))
                .andExpect(jsonPath("$[0].title", is(eventShortDto.getTitle())));

        Mockito.verify(subscriptionService).getSubscribedEventsByInitiator(userId, initiatorId, 0, 10);
    }

    @Test
    void getSubscriptionEventsByInitiatorShouldUseDefaultPagination() throws Exception {
        Mockito.when(subscriptionService.getSubscribedEventsByInitiator(anyLong(), anyLong(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/users/{userId}/subscriptions/events/{initiatorId}", userId, initiatorId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Mockito.verify(subscriptionService).getSubscribedEventsByInitiator(userId, initiatorId, 0, 10);
    }
}