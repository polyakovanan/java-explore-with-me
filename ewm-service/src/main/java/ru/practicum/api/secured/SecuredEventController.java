package ru.practicum.api.secured;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.core.persistance.model.dto.event.EventFullDto;
import ru.practicum.core.persistance.model.dto.event.EventShortDto;
import ru.practicum.core.persistance.model.dto.event.NewEventDto;
import ru.practicum.core.persistance.model.dto.event.UpdateEventUserRequest;
import ru.practicum.core.persistance.model.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.core.persistance.model.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.core.persistance.model.dto.request.ParticipationRequestDto;
import ru.practicum.core.service.EventService;
import ru.practicum.core.service.ParticipationRequestService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/users/{userId}/events")
@Validated
public class SecuredEventController {
    private final EventService eventService;
    private final ParticipationRequestService participationRequestService;

    @GetMapping
    public ResponseEntity<List<EventShortDto>> getEventsByUserId(@PathVariable Long userId,
                                                                 @RequestParam(defaultValue = "0") Integer from,
                                                                 @RequestParam(defaultValue = "10") Integer size) {
        log.info("Получен запрос GET /users/{}/events", userId);
        return ResponseEntity.ok(eventService.findByUserId(userId, from, size));
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventFullDto> getEventByUserId(@PathVariable Long userId,
                                                         @PathVariable Long eventId) {
        log.info("Получен запрос GET /users/{}/events/{}", userId, eventId);
        return ResponseEntity.ok(eventService.findByIdAndUser(userId, eventId));
    }

    @GetMapping("/{eventId}/requests")
    public ResponseEntity<List<ParticipationRequestDto>> getRequests(@PathVariable Long userId,
                                                                    @PathVariable Long eventId) {
        log.info("Получен запрос GET /users/{}/events/{}/requests", userId, eventId);
        return ResponseEntity.ok(participationRequestService.getAllByEventAndInitiator(userId, eventId));
    }

    @PostMapping()
    public ResponseEntity<EventFullDto> create(@PathVariable Long userId,
                                               @RequestBody @Valid NewEventDto eventDto) {
        log.info("Получен запрос POST /users/{}/events c новым событием: {}", userId, eventDto);
        return new ResponseEntity<>(eventService.create(userId, eventDto), HttpStatus.CREATED);
    }

    @PatchMapping("/{eventId}")
    public ResponseEntity<EventFullDto> update(@PathVariable Long userId,
                                               @PathVariable Long eventId,
                                               @RequestBody @Valid UpdateEventUserRequest eventDto) {
        log.info("Получен запрос PATCH /users/{}/events/{} c новым событием: {}", userId, eventId, eventDto);
        return ResponseEntity.ok(eventService.updateByUser(userId, eventId, eventDto));

    }

    @PatchMapping("/{eventId}/requests")
    public ResponseEntity<EventRequestStatusUpdateResult> updateRequest(@PathVariable Long userId,
                                                                        @PathVariable Long eventId,
                                                                        @RequestBody @Valid EventRequestStatusUpdateRequest eventDto) {
        log.info("Получен запрос PATCH /users/{}/events/{}/request с параметрами: {}", userId, eventId, eventDto);
        return ResponseEntity.ok(participationRequestService.updateStatus(userId, eventId, eventDto));
    }
}
