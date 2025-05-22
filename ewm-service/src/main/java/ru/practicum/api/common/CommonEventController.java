package ru.practicum.api.common;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.core.persistance.model.dto.event.EventFullDto;
import ru.practicum.core.persistance.model.dto.event.EventShortDto;
import ru.practicum.core.persistance.model.dto.event.filter.EventSearchCommon;
import ru.practicum.core.persistance.model.dto.event.filter.EventSearchOrder;
import ru.practicum.core.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/events")
@Validated
public class CommonEventController {

    private final EventService service;

    @GetMapping
    public ResponseEntity<List<EventShortDto>> findAll(@RequestParam(required = false) String text,
                                                       @RequestParam(required = false) List<Long> categories,
                                                       @RequestParam(required = false) Boolean paid,
                                                       @RequestParam(required = false)
                                                       @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                                       @RequestParam(required = false)
                                                       @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                                       @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                                       @RequestParam(defaultValue = "EVENT_DATE") String sort,
                                                       @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                                       @RequestParam(defaultValue = "10") @Positive int size) {

        EventSearchCommon eventSearchCommon = EventSearchCommon.builder()
                .text(text)
                .categories(categories)
                .paid(paid)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .onlyAvailable(onlyAvailable)
                .sort(EventSearchOrder.valueOf(sort))
                .from(from)
                .size(size)
                .build();
        log.info("Получен запрос GET /events с параметрами {}", eventSearchCommon);
        return ResponseEntity.ok(service.searchCommon(eventSearchCommon));
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventFullDto> findById(@PathVariable long eventId) {
        log.info("Получен запрос GET /events/{}", eventId);
        return ResponseEntity.ok(service.findById(eventId));
    }

}
