package ru.practicum.core.persistance.model.dto.category;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NewCategoryDto {
    @NotBlank(message = "Название категории не должно быть пустым")
    private final String name;
}
