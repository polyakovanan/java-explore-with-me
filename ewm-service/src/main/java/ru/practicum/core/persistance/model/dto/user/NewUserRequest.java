package ru.practicum.core.persistance.model.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NewUserRequest {
    @Email(message = "Некорректный формат электронной почты")
    private String email;

    @NotBlank(message = "Имя пользователя не должно быть пустым")
    private String name;
}
