package ru.practicum.core.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.client.StatsClient;
import ru.practicum.core.exception.ConditionsNotMetException;
import ru.practicum.core.exception.DateValidationException;
import ru.practicum.core.exception.NotFoundException;
import ru.practicum.core.persistance.model.*;
import ru.practicum.core.persistance.model.dto.event.*;
import ru.practicum.core.persistance.model.dto.event.filter.EventSearchAdmin;
import ru.practicum.core.persistance.model.dto.event.filter.EventSearchCommon;
import ru.practicum.core.persistance.model.dto.event.state.EventAdminStateAction;
import ru.practicum.core.persistance.model.dto.event.state.EventState;
import ru.practicum.core.persistance.model.dto.event.state.EventUserStateAction;
import ru.practicum.core.persistance.repository.CategoryRepository;
import ru.practicum.core.persistance.repository.EventRepository;
import ru.practicum.core.persistance.repository.UserRepository;
import ru.practicum.core.service.impl.EventServiceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StatsClient statsClient;

    @InjectMocks
    private EventServiceImpl eventService;

    private User user;
    private Category category;
    private Event event;
    private NewEventDto newEventDto;
    private UpdateEventAdminRequest adminRequest;
    private UpdateEventUserRequest userRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");

        category = new Category();
        category.setId(1L);
        category.setName("Test Category");

        event = new Event();
        event.setId(1L);
        event.setTitle("Test Event");
        event.setAnnotation("Test Annotation");
        event.setDescription("Test Description");
        event.setEventDate(LocalDateTime.now().plusDays(1));
        event.setInitiator(user);
        event.setCategory(category);
        event.setPaid(false);
        event.setParticipantLimit(10L);
        event.setRequestModeration(true);
        event.setState(EventState.PENDING);
        event.setCreatedOn(LocalDateTime.now());
        event.setLat(55.754167);
        event.setLon(37.620000);

        newEventDto = new NewEventDto();
        newEventDto.setTitle("New Event");
        newEventDto.setAnnotation("New Annotation");
        newEventDto.setDescription("New Description");
        newEventDto.setEventDate(LocalDateTime.now().plusDays(2));
        newEventDto.setCategory(1L);
        newEventDto.setPaid(true);
        newEventDto.setParticipantLimit(20L);
        newEventDto.setRequestModeration(false);
        newEventDto.setLocation(new Location(55.755814, 37.617635));

        adminRequest = new UpdateEventAdminRequest();
        adminRequest.setTitle("Updated Title");
        adminRequest.setStateAction(EventAdminStateAction.PUBLISH_EVENT);

        userRequest = new UpdateEventUserRequest();
        userRequest.setTitle("User Updated Title");
        userRequest.setStateAction(EventUserStateAction.SEND_TO_REVIEW);
    }

    @Test
    void findByUserIdShouldReturnEventShortDtoList() {
        when(eventRepository.findAllByInitiatorId(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(event));

        List<EventShortDto> result = eventService.findByUserId(1L, 0, 10);

        assertEquals(1, result.size());
        assertEquals(event.getTitle(), result.getFirst().getTitle());
        verify(eventRepository).findAllByInitiatorId(1L, 0, 10);
    }

    @Test
    void findByIdAndUserWhenValidShouldReturnEventFullDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        EventFullDto result = eventService.findByIdAndUser(1L, 1L);

        assertEquals(event.getTitle(), result.getTitle());
        verify(userRepository).findById(1L);
        verify(eventRepository).findById(1L);
    }

    @Test
    void findByIdAndUserWhenUserNotExistsShouldThrowNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> eventService.findByIdAndUser(1L, 1L));
        verify(userRepository).findById(1L);
        verify(eventRepository, never()).findById(anyLong());
    }

    @Test
    void findByIdAndUserWhenEventNotBelongsToUserShouldThrowConditionsNotMetException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        user.setId(2L); // Different user

        assertThrows(ConditionsNotMetException.class, () -> eventService.findByIdAndUser(1L, 1L));
    }

    @Test
    void searchCommonShouldReturnEventShortDtoList() {
        EventSearchCommon search = new EventSearchCommon();
        when(eventRepository.findCommonEventsByFilters(search)).thenReturn(List.of(event));

        List<EventShortDto> result = eventService.searchCommon(search);

        assertEquals(1, result.size());
        assertEquals(event.getTitle(), result.getFirst().getTitle());
        verify(eventRepository).findCommonEventsByFilters(search);
    }

    @Test
    void searchCommonWhenInvalidDateRangeShouldThrowDateValidationException() {
        EventSearchCommon search = new EventSearchCommon();
        search.setRangeStart(LocalDateTime.now().plusDays(1));
        search.setRangeEnd(LocalDateTime.now());

        assertThrows(DateValidationException.class, () -> eventService.searchCommon(search));
        verify(eventRepository, never()).findCommonEventsByFilters(any());
    }

    @Test
    void searchAdminShouldReturnEventFullDtoList() {
        EventSearchAdmin search = new EventSearchAdmin();
        when(eventRepository.findAdminEventsByFilters(search)).thenReturn(List.of(event));

        List<EventFullDto> result = eventService.searchAdmin(search);

        assertEquals(1, result.size());
        assertEquals(event.getTitle(), result.getFirst().getTitle());
        verify(eventRepository).findAdminEventsByFilters(search);
    }

    @Test
    void findByIdWhenPublishedShouldReturnEventFullDto() {
        event.setState(EventState.PUBLISHED);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(statsClient.getStats(anyString(), anyString(), anyList(), anyBoolean()))
                .thenReturn(List.of());

        EventFullDto result = eventService.findById(1L);

        assertEquals(event.getTitle(), result.getTitle());
        verify(eventRepository).findById(1L);
        verify(statsClient).getStats(anyString(), anyString(), anyList(), anyBoolean());
    }

    @Test
    void findByIdWhenNotPublishedShouldThrowNotFoundException() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        assertThrows(NotFoundException.class, () -> eventService.findById(1L));
    }

    @Test
    void createWhenValidShouldReturnEventFullDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        EventFullDto result = eventService.create(1L, newEventDto);

        assertEquals(event.getTitle(), result.getTitle());
        verify(userRepository).findById(1L);
        verify(categoryRepository).findById(1L);
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void createWhenEventDateTooEarlyShouldThrowDateValidationException() {
        newEventDto.setEventDate(LocalDateTime.now().plusHours(1));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        assertThrows(DateValidationException.class, () -> eventService.create(1L, newEventDto));
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void updateByAdminWhenValidShouldReturnUpdatedEvent() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        EventFullDto result = eventService.updateByAdmin(1L, adminRequest);

        assertEquals(adminRequest.getTitle(), result.getTitle());
        assertEquals(EventState.PUBLISHED, event.getState());
        verify(eventRepository).findById(1L);
        verify(eventRepository).save(event);
    }

    @Test
    void updateByAdminWhenCategoryNotExistsShouldThrowNotFoundException() {
        adminRequest.setCategory(999L);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> eventService.updateByAdmin(1L, adminRequest)
        );

        assertEquals("Категория с id=999 не найдена", exception.getMessage());
        verify(eventRepository).findById(1L);
        verify(categoryRepository).findById(999L);
        verify(eventRepository, never()).save(any());
    }

    @Test
    void updateByAdminWhenRejectPublishedEventShouldThrowConditionsNotMetException() {
        event.setState(EventState.PUBLISHED);
        adminRequest.setStateAction(EventAdminStateAction.REJECT_EVENT);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        ConditionsNotMetException exception = assertThrows(
                ConditionsNotMetException.class,
                () -> eventService.updateByAdmin(1L, adminRequest)
        );

        assertEquals("Опубликованное событие нельзя отклонить.", exception.getMessage());
        verify(eventRepository).findById(1L);
        verify(eventRepository, never()).save(any());
    }

    @Test
    void updateByAdminWhenPublishNotPendingEventShouldThrowConditionsNotMetException() {
        event.setState(EventState.CANCELED);
        adminRequest.setStateAction(EventAdminStateAction.PUBLISH_EVENT);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        ConditionsNotMetException exception = assertThrows(
                ConditionsNotMetException.class,
                () -> eventService.updateByAdmin(1L, adminRequest)
        );

        assertEquals("Опубликовать можно только событие в состоянии ожидания.", exception.getMessage());
        verify(eventRepository).findById(1L);
        verify(eventRepository, never()).save(any());
    }

    @Test
    void updateByAdminWhenEventDateTooEarlyShouldThrowDateValidationException() {
        adminRequest.setEventDate(LocalDateTime.now().plusMinutes(30));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        DateValidationException exception = assertThrows(
                DateValidationException.class,
                () -> eventService.updateByAdmin(1L, adminRequest)
        );

        assertEquals("Дата начала события должна быть не ранее чем через 1 час от даты редактирования.",
                exception.getMessage());
        verify(eventRepository).findById(1L);
        verify(eventRepository, never()).save(any());
    }

    @Test
    void updateByUserWhenValidShouldReturnUpdatedEvent() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        EventFullDto result = eventService.updateByUser(1L, 1L, userRequest);

        assertEquals(userRequest.getTitle(), result.getTitle());
        assertEquals(EventState.PENDING, event.getState());
        verify(userRepository).findById(1L);
        verify(eventRepository).findById(1L);
        verify(eventRepository).save(event);
    }

    @Test
    void updateByUserWhenCategoryNotExistsShouldThrowNotFoundException() {
        userRequest.setCategory(999L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> eventService.updateByUser(1L, 1L, userRequest)
        );

        assertEquals("Категория с id=999 не найдена", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(eventRepository).findById(1L);
        verify(categoryRepository).findById(999L);
        verify(eventRepository, never()).save(any());
    }

    @Test
    void updateByUserWhenEventDateTooEarlyShouldThrowDateValidationException() {
        userRequest.setEventDate(LocalDateTime.now().plusMinutes(30));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        DateValidationException exception = assertThrows(
                DateValidationException.class,
                () -> eventService.updateByUser(1L, 1L, userRequest)
        );

        assertEquals("Дата начала события должна быть не ранее чем через 1 час от даты редактирования.",
                exception.getMessage());
        verify(userRepository).findById(1L);
        verify(eventRepository).findById(1L);
        verify(eventRepository, never()).save(any());
    }

    @Test
    void updateByUserWhenUpdatePublishedEventShouldThrowConditionsNotMetException() {
        event.setState(EventState.PUBLISHED);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        ConditionsNotMetException exception = assertThrows(
                ConditionsNotMetException.class,
                () -> eventService.updateByUser(1L, 1L, userRequest)
        );

        assertEquals("Нельзя редактировать опубликованное событие", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(eventRepository).findById(1L);
        verify(eventRepository, never()).save(any());
    }

    @Test
    void updateByUserWhenNotInitiatorShouldThrowConditionsNotMetException() {
        // Arrange
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setName("Other User");
        otherUser.setEmail("other@example.com");

        when(userRepository.findById(2L)).thenReturn(Optional.of(otherUser));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        // Act & Assert
        ConditionsNotMetException exception = assertThrows(
                ConditionsNotMetException.class,
                () -> eventService.updateByUser(2L, 1L, userRequest)
        );

        assertEquals("Событие может редактировать только его создатель", exception.getMessage());
        verify(userRepository).findById(2L);
        verify(eventRepository).findById(1L);
        verify(eventRepository, never()).save(any());
    }
}