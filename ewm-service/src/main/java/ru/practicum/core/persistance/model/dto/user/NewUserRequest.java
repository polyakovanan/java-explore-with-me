package ru.practicum.core.persistance.model.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewUserRequest {
    @NotBlank(message = "Электронная почта не должна быть пустой")
    @Email(message = "Некорректный формат электронной почты")
    @Size(max = 254, min = 6, message = "Некорректная длина электронной почты")
    private String email;

    @NotBlank(message = "Имя пользователя не должно быть пустым")
    @Size(max = 250, min = 2, message = "Некорректная длина имени пользователя")
    private String name;
}
