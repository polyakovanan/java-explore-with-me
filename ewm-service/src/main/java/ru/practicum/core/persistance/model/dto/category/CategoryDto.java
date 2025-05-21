package ru.practicum.core.persistance.model.dto.category;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryDto {
    private final Long id;
    private final String name;
}
