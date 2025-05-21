package ru.practicum.core.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.core.exception.ConditionsNotMetException;
import ru.practicum.core.exception.NotFoundException;
import ru.practicum.core.persistance.model.Category;
import ru.practicum.core.persistance.model.Event;
import ru.practicum.core.persistance.model.User;
import ru.practicum.core.persistance.model.dto.event.*;
import ru.practicum.core.persistance.model.dto.event.filter.EventSearchAdmin;
import ru.practicum.core.persistance.model.dto.event.filter.EventSearchCommon;
import ru.practicum.core.persistance.model.dto.event.state.EventAdminStateAction;
import ru.practicum.core.persistance.model.dto.event.state.EventState;
import ru.practicum.core.persistance.model.dto.event.state.EventUserStateAction;
import ru.practicum.core.persistance.model.mapper.EventMapper;
import ru.practicum.core.persistance.repository.CategoryRepository;
import ru.practicum.core.persistance.repository.EventRepository;
import ru.practicum.core.persistance.repository.UserRepository;
import ru.practicum.core.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

@Service("eventService")
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Override
    public List<EventShortDto> findByUserId(Long userId, Integer from, Integer size) {
        return eventRepository.findAllByInitiatorId(userId, from, size)
                .stream()
                .map(EventMapper::toEventShortDto)
                .toList();

    }

    @Override
    public EventFullDto findByIdAndUser(Long userId, Long eventId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConditionsNotMetException("Просмотр полной информации о событии доступен только для создателя события");
        }

        return EventMapper.toEventFullDto(event);
    }

    @Override
    @Transactional
    public List<EventShortDto> searchCommon(EventSearchCommon search) {
        List<Event> events = eventRepository.findCommonEventsByFilters(search);
        events.forEach(event -> event.setViews(event.getViews() + 1));
        eventRepository.saveAll(events);

        return events.stream()
                .map(EventMapper::toEventShortDto)
                .toList();
    }

    @Override
    @Transactional
    public List<EventFullDto> searchAdmin(EventSearchAdmin search) {
        List<Event> events = eventRepository.findAdminEventsByFilters(search);
        events.forEach(event -> event.setViews(event.getViews() + 1));
        eventRepository.saveAll(events);

        return events.stream()
                .map(EventMapper::toEventFullDto)
                .toList();
    }

    @Override
    @Transactional
    public EventFullDto findById(Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() ->new NotFoundException("Событие с id=" + eventId + " не найдено"));
        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Событие с id=" + eventId + " не найдено");
        }

        event.setViews(event.getViews() + 1);
        eventRepository.save(event);

        return EventMapper.toEventFullDto(event);
    }

    @Override
    public EventFullDto create(Long userId, NewEventDto newEventDto) {
        User initiator = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Категория с id=" + newEventDto.getCategory() + " не найдена"));
        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConditionsNotMetException("Дата начала события должна быть не ранее чем через 2 часа от даты создания.");
        }
        return EventMapper.toEventFullDto(eventRepository.save(EventMapper.newRequestToEvent(newEventDto, initiator, category)));
    }

    @Override
    public EventFullDto updateByAdmin(long eventId, UpdateEventAdminRequest eventDto) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));
        LocalDateTime eventDate = eventDto.getEventDate() == null ? event.getEventDate() : eventDto.getEventDate();
        if (eventDate.isBefore(LocalDateTime.now().plusHours(1))) {
            throw new ConditionsNotMetException("Дата начала события должна быть не ранее чем через 1 час от даты редактирования.");
        }
        if (event.getState() == EventState.PUBLISHED && eventDto.getStateAction() == EventAdminStateAction.REJECT_EVENT) {
            throw new ConditionsNotMetException("Опубликованное событие нельзя отклонить.");
        }
        if (event.getState() != EventState.PENDING && eventDto.getStateAction() == EventAdminStateAction.PUBLISH_EVENT) {
            throw new ConditionsNotMetException("Опубликовать можно только событие в состоянии ожидания.");
        }
        if (eventDto.getCategory() != null) {
            Category category = categoryRepository.findById(eventDto.getCategory()).orElseThrow(() -> new NotFoundException("Категория с id=" + eventDto.getCategory() + " не найдена"));
            event.setCategory(category);
        }

        event.setAnnotation(eventDto.getAnnotation() == null ? event.getAnnotation() : eventDto.getAnnotation());
        event.setDescription(eventDto.getDescription() == null ? event.getDescription() : eventDto.getDescription());
        event.setEventDate(eventDate);
        event.setPaid(eventDto.getPaid() == null ? event.getPaid() : eventDto.getPaid());
        event.setParticipantLimit(eventDto.getParticipantLimit() == null ? event.getParticipantLimit() : eventDto.getParticipantLimit());
        event.setRequestModeration(eventDto.getRequestModeration() == null ? event.getRequestModeration() : eventDto.getRequestModeration());
        event.setState(eventDto.getStateAction() == null ? event.getState() :
                eventDto.getStateAction() == EventAdminStateAction.PUBLISH_EVENT ? EventState.PUBLISHED : EventState.CANCELED);
        event.setTitle(eventDto.getTitle() == null ? event.getTitle() : eventDto.getTitle());
        event.setLat(eventDto.getLocation() == null ? event.getLat() : eventDto.getLocation().getLat());
        event.setLon(eventDto.getLocation() == null ? event.getLon() : eventDto.getLocation().getLon());

        return EventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    public EventFullDto updateByUser(Long userId, Long eventId, UpdateEventUserRequest eventDto) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConditionsNotMetException("Событие может редактировать только его создатель");
        }

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConditionsNotMetException("Нельзя редактировать опубликованное событие");
        }

        LocalDateTime eventDate = eventDto.getEventDate() == null ? event.getEventDate() : eventDto.getEventDate();
        if (eventDate.isBefore(LocalDateTime.now().plusHours(1))) {
            throw new ConditionsNotMetException("Дата начала события должна быть не ранее чем через 1 час от даты редактирования.");
        }
        if (eventDto.getCategory() != null) {
            Category category = categoryRepository.findById(eventDto.getCategory()).orElseThrow(() -> new NotFoundException("Категория с id=" + eventDto.getCategory() + " не найдена"));
            event.setCategory(category);
        }

        if (eventDto.getStateAction() == EventUserStateAction.SEND_TO_REVIEW) {
            event.setState(EventState.PENDING);
        }
        if (eventDto.getStateAction() == EventUserStateAction.CANCEL_REVIEW) {
            event.setState(EventState.CANCELED);
        }
        event.setAnnotation(eventDto.getAnnotation() == null ? event.getAnnotation() : eventDto.getAnnotation());
        event.setDescription(eventDto.getDescription() == null ? event.getDescription() : eventDto.getDescription());
        event.setEventDate(eventDate);
        event.setPaid(eventDto.getPaid() == null ? event.getPaid() : eventDto.getPaid());
        event.setParticipantLimit(eventDto.getParticipantLimit() == null ? event.getParticipantLimit() : eventDto.getParticipantLimit());
        event.setRequestModeration(eventDto.getRequestModeration() == null ? event.getRequestModeration() : eventDto.getRequestModeration());
        event.setTitle(eventDto.getTitle() == null ? event.getTitle() : eventDto.getTitle());
        event.setLat(eventDto.getLocation() == null ? event.getLat() : eventDto.getLocation().getLat());
        event.setLon(eventDto.getLocation() == null ? event.getLon() : eventDto.getLocation().getLon());

        return EventMapper.toEventFullDto(eventRepository.save(event));
    }
}
