package ru.practicum.core.persistance.model.dto.compilation;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class NewCompilationDto {
    @NotBlank(message = "Название подборки не может быть пустым")
    private String title;
    private Boolean pinned;
    private Set<Long> events;
}
