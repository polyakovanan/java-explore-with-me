package ru.practicum.core.service;

import ru.practicum.core.persistance.model.dto.user.NewUserRequest;
import ru.practicum.core.persistance.model.dto.user.UserDto;

import java.util.List;

public interface UserService {
    List<UserDto> getAll(List<Long> ids, Integer from, Integer size);

    UserDto create(NewUserRequest user);

    void delete(Long userId);
}
