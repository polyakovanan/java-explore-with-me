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
import ru.practicum.core.persistance.model.*;
import ru.practicum.core.persistance.model.dto.event.*;
import ru.practicum.core.persistance.model.dto.event.state.EventAdminStateAction;
import ru.practicum.core.persistance.model.dto.event.state.EventState;
import ru.practicum.core.persistance.model.dto.event.state.EventUserStateAction;
import ru.practicum.core.persistance.model.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.core.persistance.model.dto.request.ParticipationRequestStatus;
import ru.practicum.core.persistance.repository.*;

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
class EventIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ParticipationRequestRepository requestRepository;

    private User initiator;
    private User participant;
    private Category category;
    private Event event;
    private NewEventDto newEventDto;
    private UpdateEventAdminRequest adminUpdateRequest;
    private UpdateEventUserRequest userUpdateRequest;

    @BeforeEach
    void setUp() {
        requestRepository.deleteAll();
        eventRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        initiator = userRepository.save(User.builder()
                .name("Initiator")
                .email("initiator@example.com")
                .build());

        participant = userRepository.save(User.builder()
                .name("Participant")
                .email("participant@example.com")
                .build());

        category = categoryRepository.save(Category.builder()
                .name("Test Category")
                .build());

        event = eventRepository.save(Event.builder()
                .title("Test Event")
                .annotation("Test Annotation")
                .description("Test Description")
                .eventDate(LocalDateTime.now().plusDays(1))
                .initiator(initiator)
                .category(category)
                .paid(false)
                .participantLimit(10L)
                .requestModeration(true)
                .state(EventState.PENDING)
                .createdOn(LocalDateTime.now())
                .confirmedRequests(0L)
                .lat(55.754167)
                .lon(37.620000)
                .build());

        newEventDto = NewEventDto.builder()
                .title("New Event")
                .annotation("Detailed annotation for new event")
                .description("Full description of the new event with all details")
                .eventDate(LocalDateTime.now().plusDays(2))
                .category(category.getId())
                .paid(true)
                .participantLimit(20L)
                .requestModeration(false)
                .location(new Location(55.755814, 37.617635))
                .build();

        adminUpdateRequest = UpdateEventAdminRequest.builder()
                .title("Admin Updated Title")
                .stateAction(EventAdminStateAction.PUBLISH_EVENT)
                .build();

        userUpdateRequest = UpdateEventUserRequest.builder()
                .title("User Updated Title")
                .stateAction(EventUserStateAction.SEND_TO_REVIEW)
                .build();
    }

    @Test
    void createEventThroughSecuredEndpointShouldCreateAndReturnEvent() throws Exception {
        mockMvc.perform(post("/users/{userId}/events", initiator.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.title").value(newEventDto.getTitle()))
                .andExpect(jsonPath("$.state").value(EventState.PENDING.toString()));

        List<Event> events = eventRepository.findAll();
        assertEquals(2, events.size());
        assertTrue(events.stream().anyMatch(e -> e.getTitle().equals(newEventDto.getTitle())));
    }

    @Test
    void updateEventThroughAdminEndpointShouldPublishEvent() throws Exception {
        mockMvc.perform(patch("/admin/events/{eventId}", event.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(adminUpdateRequest.getTitle()))
                .andExpect(jsonPath("$.state").value(EventState.PUBLISHED.toString()));

        Event updatedEvent = eventRepository.findById(event.getId()).orElseThrow();
        assertEquals(EventState.PUBLISHED, updatedEvent.getState());
    }

    @Test
    void updateEventThroughUserEndpointShouldUpdateEvent() throws Exception {
        mockMvc.perform(patch("/users/{userId}/events/{eventId}", initiator.getId(), event.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(userUpdateRequest.getTitle()));

        Event updatedEvent = eventRepository.findById(event.getId()).orElseThrow();
        assertEquals(userUpdateRequest.getTitle(), updatedEvent.getTitle());
    }

    @Test
    void getPublishedEventsThroughCommonEndpointShouldReturnOnlyPublished() throws Exception {
        event.setState(EventState.PUBLISHED);
        eventRepository.save(event);

        eventRepository.save(Event.builder()
                .title("Test Event")
                .annotation("Test Annotation")
                .description("Test Description")
                .eventDate(LocalDateTime.now().plusDays(1))
                .initiator(initiator)
                .category(category)
                .paid(false)
                .participantLimit(10L)
                .requestModeration(true)
                .state(EventState.PENDING)
                .createdOn(LocalDateTime.now())
                .lat(55.754167)
                .lon(37.620000)
                .build());

        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value(event.getTitle()));
    }

    @Test
    void getEventRequestsThroughSecuredEndpointShouldReturnRequests() throws Exception {
        ParticipationRequest request = requestRepository.save(ParticipationRequest.builder()
                .event(event)
                .requester(participant)
                .status(ParticipationRequestStatus.PENDING)
                .created(LocalDateTime.now())
                .build());

        mockMvc.perform(get("/users/{userId}/events/{eventId}/requests", initiator.getId(), event.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(request.getId()));
    }

    @Test
    void updateRequestStatusThroughSecuredEndpointShouldUpdateStatus() throws Exception {
        ParticipationRequest request = requestRepository.save(ParticipationRequest.builder()
                .event(event)
                .requester(participant)
                .status(ParticipationRequestStatus.PENDING)
                .created(LocalDateTime.now())
                .build());

        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();
        updateRequest.setRequestIds(Set.of(request.getId()));
        updateRequest.setStatus(ParticipationRequestStatus.CONFIRMED);

        mockMvc.perform(patch("/users/{userId}/events/{eventId}/requests", initiator.getId(), event.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmedRequests", hasSize(1)));

        ParticipationRequest updatedRequest = requestRepository.findById(request.getId()).orElseThrow();
        assertEquals(ParticipationRequestStatus.CONFIRMED, updatedRequest.getStatus());
    }

    @Test
    void createEventWithInvalidDataShouldReturnBadRequest() throws Exception {
        NewEventDto invalidDto = NewEventDto.builder()
                .title("Short")
                .build();

        mockMvc.perform(post("/users/{userId}/events", initiator.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }
}