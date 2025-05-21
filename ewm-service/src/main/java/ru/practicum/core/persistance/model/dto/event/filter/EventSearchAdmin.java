package ru.practicum.core.persistance.model.dto.event.filter;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class EventSearchAdmin {
    private List<Long> users;
    private List<String> states;
    private List<Long> categories;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private Integer from;
    private Integer size;
}
