package ru.practicum.core.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.core.exception.ConditionsNotMetException;
import ru.practicum.core.exception.NotFoundException;
import ru.practicum.core.persistance.model.Subscription;
import ru.practicum.core.persistance.model.User;
import ru.practicum.core.persistance.model.dto.event.EventShortDto;
import ru.practicum.core.persistance.model.dto.user.UserShortDto;
import ru.practicum.core.persistance.repository.EventRepository;
import ru.practicum.core.persistance.repository.SubscriptionRepository;
import ru.practicum.core.persistance.repository.UserRepository;
import ru.practicum.core.service.impl.SubscriptionServiceImpl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    private final Long userId = 1L;
    private final Long initiatorId = 2L;
    private final Long subscriberId = 3L;
    private User user;
    private User initiator;
    private Subscription subscription;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(userId);

        initiator = new User();
        initiator.setId(initiatorId);
        initiator.setSubscribers(1L);

        subscription = new Subscription();
        subscription.setId(new Subscription.SubscriptionId(user, initiator));
    }

    @Test
    void getSubscribedEventsWhenUserNotFoundShouldThrowException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> subscriptionService.getSubscribedEvents(userId, 0, 10));

        verify(userRepository).findById(userId);
        verifyNoInteractions(subscriptionRepository, eventRepository);
    }

    @Test
    void getSubscribedEventsWhenNoSubscriptionsShouldReturnEmptyList() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(subscriptionRepository.findAllByIdSubscriberId(userId)).thenReturn(Collections.emptyList());

        List<EventShortDto> result = subscriptionService.getSubscribedEvents(userId, 0, 10);

        assertTrue(result.isEmpty());
        verify(userRepository).findById(userId);
        verify(subscriptionRepository).findAllByIdSubscriberId(userId);
        verifyNoInteractions(eventRepository);
    }

    @Test
    void getSubscribedEventsByInitiatorWhenUserNotFoundShouldThrowException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> subscriptionService.getSubscribedEventsByInitiator(userId, initiatorId, 0, 10));

        verify(userRepository).findById(userId);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(subscriptionRepository, eventRepository);
    }

    @Test
    void getSubscribedEventsByInitiatorWhenInitiatorNotFoundShouldThrowException() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(initiatorId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> subscriptionService.getSubscribedEventsByInitiator(userId, initiatorId, 0, 10));

        verify(userRepository).findById(userId);
        verify(userRepository).findById(initiatorId);
        verifyNoInteractions(subscriptionRepository, eventRepository);
    }

    @Test
    void getSubscribedEventsByInitiatorWhenNotSubscribedShouldThrowException() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(initiatorId)).thenReturn(Optional.of(initiator));
        when(subscriptionRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(ConditionsNotMetException.class,
                () -> subscriptionService.getSubscribedEventsByInitiator(userId, initiatorId, 0, 10));

        verify(userRepository).findById(userId);
        verify(userRepository).findById(initiatorId);
        verify(subscriptionRepository).findById(any());
        verifyNoInteractions(eventRepository);
    }

    @Test
    void getSubscribersWhenUserNotFoundShouldThrowException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> subscriptionService.getSubscribers(userId));

        verify(userRepository).findById(userId);
        verifyNoInteractions(subscriptionRepository);
    }

    @Test
    void getSubscriptionsWhenUserNotFoundShouldThrowException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> subscriptionService.getSubscriptions(userId));

        verify(userRepository).findById(userId);
        verifyNoInteractions(subscriptionRepository);
    }

    @Test
    void subscribeWhenUserNotFoundShouldThrowException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> subscriptionService.subscribe(userId, initiatorId));

        verify(userRepository).findById(userId);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(subscriptionRepository);
    }

    @Test
    void subscribeWhenInitiatorNotFoundShouldThrowException() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(initiatorId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> subscriptionService.subscribe(userId, initiatorId));

        verify(userRepository).findById(userId);
        verify(userRepository).findById(initiatorId);
        verifyNoInteractions(subscriptionRepository);
    }

    @Test
    void subscribeWhenAlreadySubscribedShouldThrowException() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(initiatorId)).thenReturn(Optional.of(initiator));
        when(subscriptionRepository.findById(any())).thenReturn(Optional.of(subscription));

        assertThrows(ConditionsNotMetException.class,
                () -> subscriptionService.subscribe(userId, initiatorId));

        verify(userRepository).findById(userId);
        verify(userRepository).findById(initiatorId);
        verify(subscriptionRepository).findById(any());
        verifyNoMoreInteractions(subscriptionRepository);
    }

    @Test
    void subscribeWhenValidDataShouldCreateSubscription() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(initiatorId)).thenReturn(Optional.of(initiator));
        when(subscriptionRepository.findById(any())).thenReturn(Optional.empty());
        when(subscriptionRepository.save(any())).thenReturn(subscription);

        subscriptionService.subscribe(userId, initiatorId);

        verify(userRepository).findById(userId);
        verify(userRepository).findById(initiatorId);
        verify(subscriptionRepository).findById(any());
        verify(subscriptionRepository).save(any());
        verify(userRepository).save(initiator);
        assertEquals(2, initiator.getSubscribers());
    }

    @Test
    void unsubscribeWhenUserNotFoundShouldThrowException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> subscriptionService.unsubscribe(userId, initiatorId));

        verify(userRepository).findById(userId);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(subscriptionRepository);
    }

    @Test
    void unsubscribeWhenInitiatorNotFoundShouldThrowException() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(initiatorId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> subscriptionService.unsubscribe(userId, initiatorId));

        verify(userRepository).findById(userId);
        verify(userRepository).findById(initiatorId);
        verifyNoInteractions(subscriptionRepository);
    }

    @Test
    void unsubscribeWhenSubscriptionNotFoundShouldThrowException() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(initiatorId)).thenReturn(Optional.of(initiator));
        when(subscriptionRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> subscriptionService.unsubscribe(userId, initiatorId));

        verify(userRepository).findById(userId);
        verify(userRepository).findById(initiatorId);
        verify(subscriptionRepository).findById(any());
        verifyNoMoreInteractions(subscriptionRepository);
    }

    @Test
    void unsubscribeWhenValidDataShouldDeleteSubscription() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(initiatorId)).thenReturn(Optional.of(initiator));
        when(subscriptionRepository.findById(any())).thenReturn(Optional.of(subscription));

        subscriptionService.unsubscribe(userId, initiatorId);

        verify(userRepository).findById(userId);
        verify(userRepository).findById(initiatorId);
        verify(subscriptionRepository).findById(any());
        verify(subscriptionRepository).delete(subscription);
        verify(userRepository).save(initiator);
        assertEquals(0, initiator.getSubscribers());
    }

    @Test
    void removeWhenUserNotFoundShouldThrowException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> subscriptionService.remove(userId, subscriberId));

        verify(userRepository).findById(userId);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(subscriptionRepository);
    }

    @Test
    void removeWhenSubscriberNotFoundShouldThrowException() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(subscriberId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> subscriptionService.remove(userId, subscriberId));

        verify(userRepository).findById(userId);
        verify(userRepository).findById(subscriberId);
        verifyNoInteractions(subscriptionRepository);
    }

    @Test
    void removeWhenSubscriptionNotFoundShouldThrowException() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(subscriberId)).thenReturn(Optional.of(initiator));
        when(subscriptionRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> subscriptionService.remove(userId, subscriberId));

        verify(userRepository).findById(userId);
        verify(userRepository).findById(subscriberId);
        verify(subscriptionRepository).findById(any());
        verifyNoMoreInteractions(subscriptionRepository);
    }

    @Test
    void removeWhenValidDataShouldDeleteSubscription() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(subscriberId)).thenReturn(Optional.of(initiator));
        when(subscriptionRepository.findById(any())).thenReturn(Optional.of(subscription));

        subscriptionService.remove(subscriberId, userId);

        verify(userRepository).findById(userId);
        verify(userRepository).findById(subscriberId);
        verify(subscriptionRepository).findById(any());
        verify(subscriptionRepository).delete(subscription);
        verify(userRepository).save(initiator);
        assertEquals(0, initiator.getSubscribers());
    }

    @Test
    void getSubscribedEventsWhenSubscriptionsExistShouldReturnEvents() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(subscriptionRepository.findAllByIdSubscriberId(userId)).thenReturn(List.of(subscription));
        when(eventRepository.findAllByInitiatorIdIn(anyList(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        List<EventShortDto> result = subscriptionService.getSubscribedEvents(userId, 0, 10);

        assertNotNull(result);
        verify(userRepository).findById(userId);
        verify(subscriptionRepository).findAllByIdSubscriberId(userId);
        verify(eventRepository).findAllByInitiatorIdIn(anyList(), anyInt(), anyInt());
    }

    @Test
    void getSubscribedEventsByInitiatorWhenValidDataShouldReturnEvents() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(initiatorId)).thenReturn(Optional.of(initiator));
        when(subscriptionRepository.findById(any())).thenReturn(Optional.of(subscription));
        when(eventRepository.findAllByInitiatorIdIn(anyList(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        List<EventShortDto> result = subscriptionService.getSubscribedEventsByInitiator(userId, initiatorId, 0, 10);

        assertNotNull(result);
        verify(userRepository).findById(userId);
        verify(userRepository).findById(initiatorId);
        verify(subscriptionRepository).findById(any());
        verify(eventRepository).findAllByInitiatorIdIn(anyList(), anyInt(), anyInt());
    }

    @Test
    void getSubscribersWhenSubscribersExistShouldReturnSubscribers() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(subscriptionRepository.findAllByIdSubscribedId(userId)).thenReturn(List.of(subscription));

        List<UserShortDto> result = subscriptionService.getSubscribers(userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRepository).findById(userId);
        verify(subscriptionRepository).findAllByIdSubscribedId(userId);
    }

    @Test
    void getSubscriptionsWhenSubscriptionsExistShouldReturnSubscriptions() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(subscriptionRepository.findAllByIdSubscriberId(userId)).thenReturn(List.of(subscription));

        List<UserShortDto> result = subscriptionService.getSubscriptions(userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRepository).findById(userId);
        verify(subscriptionRepository).findAllByIdSubscriberId(userId);
    }
}