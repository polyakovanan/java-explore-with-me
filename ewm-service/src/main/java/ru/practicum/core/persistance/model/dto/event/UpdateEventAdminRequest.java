package ru.practicum.core.persistance.model.dto.event;

import lombok.Builder;
import lombok.Data;
import ru.practicum.core.persistance.model.dto.event.state.EventAdminStateAction;

import java.time.LocalDateTime;

@Data
@Builder
public class UpdateEventAdminRequest {
    private String annotation;
    private Long category;
    private String description;
    private LocalDateTime eventDate;
    private Location location;
    private Boolean paid;
    private Long participantLimit;
    private Boolean requestModeration;
    private String title;
    private EventAdminStateAction stateAction;
}
