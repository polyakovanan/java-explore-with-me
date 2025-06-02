package ru.practicum.core.service;

import ru.practicum.core.persistance.model.dto.event.EventShortDto;
import ru.practicum.core.persistance.model.dto.user.UserShortDto;

import java.util.List;

public interface SubscriptionService {
    List<EventShortDto> getSubscribedEvents(Long userId, Integer from, Integer size);

    List<EventShortDto> getSubscribedEventsByInitiator(Long userId, Long initiatorId, Integer from, Integer size);

    List<UserShortDto> getSubscribers(Long userId);

    List<UserShortDto> getSubscriptions(Long userId);

    void subscribe(Long userId, Long initiatorId);

    void unsubscribe(Long userId, Long initiatorId);

    void remove(Long userId, Long subscriberId);
}
