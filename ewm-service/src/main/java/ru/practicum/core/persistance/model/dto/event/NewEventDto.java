package ru.practicum.core.persistance.model.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewEventDto {
    @NotBlank(message = "Аннотация события не должен быть пустым")
    @Size(min = 20, max = 2000, message = "Описание события должно быть от 20 до 2000 символов")
    private String annotation;
    @NotNull(message = "У события должна быть указана категория")
    private Long category;
    @NotBlank(message = "Описание события не должно быть пустым")
    @Size(min = 20, max = 7000, message = "Описание события должно быть от 20 до 7000 символов")
    private String description;
    @NotNull(message = "У события должна быть указана дата и время на которые намечено событие")
    @Future(message = "Нельзя создать событие на прошедшую дату")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;
    @NotNull(message = "Место проведения события должно быть указано")
    private Location location;
    private Boolean paid;
    @PositiveOrZero(message = "Лимит участников не может быть отрицательным")
    private Long participantLimit;
    private Boolean requestModeration;
    @NotBlank(message = "Название события не должно быть пустым")
    @Size(min = 3, max = 120, message = "Название события должно быть от 3 до 120 символов")
    private String title;
}
