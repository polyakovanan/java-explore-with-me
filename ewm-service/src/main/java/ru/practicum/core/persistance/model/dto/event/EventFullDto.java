package ru.practicum.core.persistance.model.dto.event;

import lombok.Builder;
import lombok.Data;
import ru.practicum.core.persistance.model.dto.category.CategoryDto;
import ru.practicum.core.persistance.model.dto.event.state.EventState;
import ru.practicum.core.persistance.model.dto.user.UserShortDto;

import java.time.LocalDateTime;

@Data
@Builder
public class EventFullDto {
    private Long id;
    private String annotation;
    private CategoryDto category;
    private Long confirmedRequests;
    private LocalDateTime createdOn;
    private String description;
    private LocalDateTime eventDate;
    private UserShortDto initiator;
    private Location location;
    private Boolean paid;
    private Long participantLimit;
    private LocalDateTime publishedOn;
    private Boolean requestModeration;
    private EventState state;
    private String title;
    private Long views;
}
