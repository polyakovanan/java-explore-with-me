package ru.practicum.core.persistance.model.dto.event;

import lombok.Builder;
import lombok.Data;
import ru.practicum.core.persistance.model.dto.category.CategoryDto;
import ru.practicum.core.persistance.model.dto.user.UserShortDto;

import java.time.LocalDateTime;

@Data
@Builder
public class EventShortDto {
    private Long id;

    private String annotation;

    private CategoryDto category;

    private Long confirmedRequests;

    private LocalDateTime eventDate;

    private UserShortDto initiator;

    private Boolean paid;

    private String title;

    private Long views;
}
