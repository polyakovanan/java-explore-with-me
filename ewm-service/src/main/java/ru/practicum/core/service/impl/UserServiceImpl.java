package ru.practicum.core.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.core.exception.ConditionsNotMetException;
import ru.practicum.core.exception.NotFoundException;
import ru.practicum.core.persistance.model.User;
import ru.practicum.core.persistance.model.mapper.UserMapper;
import ru.practicum.core.persistance.model.dto.user.NewUserRequest;
import ru.practicum.core.persistance.model.dto.user.UserDto;
import ru.practicum.core.persistance.repository.UserRepository;
import ru.practicum.core.service.UserService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public List<UserDto> getAll(List<Long> ids, Integer from, Integer size) {
        return userRepository.findUsers(ids, from, size).stream()
                .map(UserMapper::userToDto)
                .toList();
    }

    @Override
    public UserDto create(NewUserRequest user) {
        Optional<User> userByEmail = userRepository.findByEmail(user.getEmail());
        if (userByEmail.isPresent()) {
            throw new ConditionsNotMetException("Пользователь с таким email уже существует");
        }
        return UserMapper.userToDto(
                userRepository.save(UserMapper.requestToUser(user))
        );
    }

    @Override
    public void delete(Long userId) {
        userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id=" + userId + " не найден")
        );
        userRepository.deleteById(userId);
    }
}
