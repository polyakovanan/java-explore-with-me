package ru.practicum.core.persistance.model.dto.compilation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewCompilationDto {
    @NotBlank(message = "Название подборки не может быть пустым")
    @Size(max = 50, message = "Название подборки не должно превышать 50 символов")
    private String title;
    private Boolean pinned;
    private Set<Long> events;
}
