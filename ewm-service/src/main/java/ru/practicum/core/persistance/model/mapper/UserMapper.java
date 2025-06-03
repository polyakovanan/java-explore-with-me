package ru.practicum.core.persistance.model.mapper;

import ru.practicum.core.persistance.model.User;
import ru.practicum.core.persistance.model.dto.user.NewUserRequest;
import ru.practicum.core.persistance.model.dto.user.UserDto;
import ru.practicum.core.persistance.model.dto.user.UserShortDto;

public class UserMapper {

    private UserMapper() {

    }

    public static User requestToUser(NewUserRequest userRequest) {
        return User.builder()
                .name(userRequest.getName())
                .email(userRequest.getEmail())
                .subscribers(0L)
                .build();
    }

    public static UserDto userToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .subscribers(user.getSubscribers())
                .build();
    }

    public static UserShortDto userToShortDto(User user) {
        return UserShortDto.builder()
                .id(user.getId())
                .name(user.getName())
                .subscribers(user.getSubscribers())
                .build();
    }
}
