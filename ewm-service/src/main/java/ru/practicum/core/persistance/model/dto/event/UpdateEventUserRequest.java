package ru.practicum.core.persistance.model.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.core.persistance.model.dto.event.state.EventUserStateAction;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventUserRequest {
   @Size(min = 20, max = 2000, message = "Описание события должно быть от 20 до 2000 символов")
   private String annotation;
   private Long category;
   @Size(min = 20, max = 7000, message = "Описание события должно быть от 20 до 7000 символов")
   private String description;
   @Future(message = "Нельзя обновить событие на прошедшую дату")
   @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
   private LocalDateTime eventDate;
   private Location location;
   private Boolean paid;
   @PositiveOrZero(message = "Лимит участников не может быть отрицательным")
   private Long participantLimit;
   private Boolean requestModeration;
   private EventUserStateAction stateAction;
   @Size(min = 3, max = 120, message = "Название события должно быть от 3 до 120 символов")
   private String title;
}
