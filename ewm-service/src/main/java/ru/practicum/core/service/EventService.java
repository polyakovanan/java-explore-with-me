package ru.practicum.core.service;

import ru.practicum.core.persistance.model.dto.event.*;
import ru.practicum.core.persistance.model.dto.event.filter.EventSearchAdmin;
import ru.practicum.core.persistance.model.dto.event.filter.EventSearchCommon;

import java.util.List;

public interface EventService {
    List<EventShortDto> searchCommon(EventSearchCommon search);

    List<EventFullDto> searchAdmin(EventSearchAdmin search);

    EventFullDto findById(Long eventId);

    EventFullDto create(Long userId, NewEventDto event);

    EventFullDto updateByAdmin(long eventId, UpdateEventAdminRequest eventDto);

    List<EventShortDto> findByUserId(Long userId, Integer from, Integer size);

    EventFullDto findByIdAndUser(Long userId, Long eventId);

    EventFullDto updateByUser(Long userId, Long eventId, UpdateEventUserRequest eventDto);
}
