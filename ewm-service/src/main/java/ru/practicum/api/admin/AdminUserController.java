package ru.practicum.api.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.core.persistance.model.dto.user.NewUserRequest;
import ru.practicum.core.persistance.model.dto.user.UserDto;
import ru.practicum.core.service.UserService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin/users")
@Validated
public class AdminUserController {

    private final UserService service;

    @GetMapping()
    public ResponseEntity<List<UserDto>> getAll(@RequestParam(required = false) List<Long> ids,
                                                @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                                @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("Получен запрос GET /admin/users");
        return new ResponseEntity<>(service.getAll(ids, from, size), HttpStatus.OK);
    }

    @PostMapping()
    public ResponseEntity<UserDto> create(@RequestBody @Valid NewUserRequest userRequest) {
        log.info("Получен запрос POST /admin/users c новым пользователем: {}", userRequest);
        return new ResponseEntity<>(service.create(userRequest), HttpStatus.CREATED);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> delete(@PathVariable Long userId) {
        log.info("Получен запрос DELETE /admin/users/{}", userId);
        service.delete(userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


}
