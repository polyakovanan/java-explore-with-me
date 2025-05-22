package ru.practicum.api.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.core.persistance.model.dto.event.EventFullDto;
import ru.practicum.core.persistance.model.dto.event.UpdateEventAdminRequest;
import ru.practicum.core.persistance.model.dto.event.filter.EventSearchAdmin;
import ru.practicum.core.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin/events")
@Validated
public class AdminEventController {

    private final EventService eventService;

    @GetMapping()
    public ResponseEntity<List<EventFullDto>> getAll(@RequestParam(required = false) List<Long> users,
                                                     @RequestParam(required = false) List<String> states,
                                                     @RequestParam(required = false) List<Long> categories,
                                                     @RequestParam(required = false)
                                                     @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                                     @RequestParam(required = false)
                                                     @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                                     @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                                     @RequestParam(defaultValue = "10") @Positive int size) {

        EventSearchAdmin search = EventSearchAdmin.builder()
                .users(users)
                .states(states)
                .categories(categories)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .from(from)
                .size(size)
                .build();

        log.info("Получен запрос GET /admin/events с параметрами {}", search);
        return ResponseEntity.ok(eventService.searchAdmin(search));
    }

    @PatchMapping("/{eventId}")
    public ResponseEntity<EventFullDto> update(@PathVariable long eventId,
                                               @RequestBody @Valid UpdateEventAdminRequest eventDto) {
        log.info("Получен запрос PATCH /admin/events/{} с параметрами {}", eventId, eventDto);
        return ResponseEntity.ok(eventService.updateByAdmin(eventId, eventDto));
    }
}
