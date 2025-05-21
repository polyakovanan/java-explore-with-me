package ru.practicum.core.persistance.model.dto.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NewEventDto {
    @NotBlank(message = "Аннотация события не должен быть пустым")
    private String annotation;
    @NotBlank(message = "У события должна быть указана категория")
    private Long category;
    @NotBlank(message = "Описание события не должно быть пустым")
    private String description;
    @NotNull(message = "У события должна быть указана дата и время на которые намечено событие")
    private LocalDateTime eventDate;
    @NotBlank(message = "Место проведения события должно быть указано")
    private Location location;
    private Boolean paid;
    private Long participantLimit;
    private Boolean requestModeration;
    @NotBlank(message = "Название события не должно быть пустым")
    private String title;
}
