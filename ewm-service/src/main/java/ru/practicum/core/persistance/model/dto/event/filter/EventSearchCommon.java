package ru.practicum.core.persistance.model.dto.event.filter;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class EventSearchCommon {
    private String text;
    private Boolean paid;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private List<Long> categories;
    private Boolean onlyAvailable;
    private EventSearchOrder sort;
    private Integer from;
    private Integer size;
}
