package ru.practicum.core.persistance.model.mapper;

import ru.practicum.core.persistance.model.Category;
import ru.practicum.core.persistance.model.Event;
import ru.practicum.core.persistance.model.User;
import ru.practicum.core.persistance.model.dto.event.EventFullDto;
import ru.practicum.core.persistance.model.dto.event.EventShortDto;
import ru.practicum.core.persistance.model.dto.event.Location;
import ru.practicum.core.persistance.model.dto.event.NewEventDto;
import ru.practicum.core.persistance.model.dto.event.state.EventState;

import java.time.LocalDateTime;

public class EventMapper {
    private EventMapper() {

    }

    public static EventFullDto toEventFullDto(Event event) {
        return EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.categoryToDto(event.getCategory()))
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .initiator(UserMapper.userToShortDto(event.getInitiator()))
                .location(new Location(event.getLat(), event.getLon()))
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .confirmedRequests(event.getConfirmedRequests())
                .publishedOn(event.getPublishedOn())
                .requestModeration(event.getRequestModeration())
                .state(event.getState())
                .title(event.getTitle())
                .views(event.getViews())
                .build();
    }

    public static EventShortDto toEventShortDto(Event event) {
        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.categoryToDto(event.getCategory()))
                .eventDate(event.getEventDate())
                .confirmedRequests(event.getConfirmedRequests())
                .initiator(UserMapper.userToShortDto(event.getInitiator()))
                .paid(event.getPaid())
                .title(event.getTitle())
                .views(event.getViews())
                .build();
    }

    public static Event newRequestToEvent(NewEventDto newEventDto, User user, Category category) {
        return Event.builder()
                .initiator(user)
                .category(category)
                .title(newEventDto.getTitle())
                .paid(newEventDto.getPaid() != null && newEventDto.getPaid())
                .requestModeration(newEventDto.getRequestModeration() == null || newEventDto.getRequestModeration())
                .participantLimit(newEventDto.getParticipantLimit() == null ? 0 : newEventDto.getParticipantLimit())
                .lon(newEventDto.getLocation().getLon())
                .lat(newEventDto.getLocation().getLat())
                .annotation(newEventDto.getAnnotation())
                .eventDate(newEventDto.getEventDate())
                .description(newEventDto.getDescription())
                .createdOn(LocalDateTime.now())
                .state(EventState.PENDING)
                .confirmedRequests(0L)
                .views(0L)
                .build();
    }
}
