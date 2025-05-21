package ru.practicum.core.persistance.model.dto.compilation;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class UpdateCompilationRequest {
    private Boolean pinned;
    private String title;
    private Set<Long> events;
}
