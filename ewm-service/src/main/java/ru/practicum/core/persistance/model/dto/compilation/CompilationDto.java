package ru.practicum.core.persistance.model.dto.compilation;

import lombok.Builder;
import lombok.Data;
import ru.practicum.core.persistance.model.dto.event.EventShortDto;

import java.util.List;

@Data
@Builder
public class CompilationDto {
    private Long id;
    private String title;
    private Boolean pinned;
    private List<EventShortDto> events;
}
