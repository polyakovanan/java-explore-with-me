package ru.practicum.api.secured;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.core.persistance.model.dto.event.EventShortDto;
import ru.practicum.core.persistance.model.dto.user.UserShortDto;
import ru.practicum.core.service.SubscriptionService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/users/{userId}/subscriptions")
@Validated
public class SecuredSubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping
    public ResponseEntity<List<UserShortDto>> getSubscriptions(@PathVariable Long userId) {
        log.info("Получен запрос GET /users/{}/subscriptions", userId);
        return ResponseEntity.ok(subscriptionService.getSubscriptions(userId));
    }

    @GetMapping("/subscribers")
    public ResponseEntity<List<UserShortDto>> getSubscribers(@PathVariable Long userId) {
        log.info("Получен запрос GET /users/{}/subscriptions/events/subscribers", userId);
        return ResponseEntity.ok(subscriptionService.getSubscribers(userId));
    }

    @PostMapping("/{initiatorId}")
    public ResponseEntity<Map<String, Object>> subscribe(@PathVariable Long userId, @PathVariable Long initiatorId) {
        subscriptionService.subscribe(userId, initiatorId);
        log.info("Получен запрос POST /users/{}/subscriptions/{}", userId, initiatorId);
        return new ResponseEntity<>(Collections.emptyMap(), HttpStatus.CREATED);
    }

    @PatchMapping("/{initiatorId}/cancel")
    public ResponseEntity<Map<String, Object>> cancel(@PathVariable Long userId, @PathVariable Long initiatorId) {
        subscriptionService.unsubscribe(userId, initiatorId);
        log.info("Получен запрос PATCH /users/{}/subscriptions/{}/cancel", userId, initiatorId);
        return new ResponseEntity<>(Collections.emptyMap(), HttpStatus.NO_CONTENT);
    }

    @PatchMapping("/{subscriberId}/remove")
    public ResponseEntity<Map<String, Object>> remove(@PathVariable Long userId, @PathVariable Long subscriberId) {
        subscriptionService.remove(userId, subscriberId);
        log.info("Получен запрос PATCH /users/{}/subscriptions/{}/remove", userId, subscriberId);
        return new ResponseEntity<>(Collections.emptyMap(), HttpStatus.NO_CONTENT);
    }

    @GetMapping("/events")
    public ResponseEntity<List<EventShortDto>> getSubscriptionEvents(@PathVariable Long userId,
                                                                     @RequestParam(defaultValue = "0") Integer from,
                                                                     @RequestParam(defaultValue = "10") Integer size) {
        log.info("Получен запрос GET /users/{}/subscriptions/events", userId);
        return ResponseEntity.ok(subscriptionService.getSubscribedEvents(userId, from, size));
    }

    @GetMapping("/events/{initiatorId}")
    public ResponseEntity<List<EventShortDto>> getSubscriptionEventsByInitiator(@PathVariable Long userId,
                                                                         @PathVariable Long initiatorId,
                                                                         @RequestParam(defaultValue = "0") Integer from,
                                                                         @RequestParam(defaultValue = "10") Integer size) {
        log.info("Получен запрос GET /users/{}/subscriptions/events/{}", userId, initiatorId);
        return ResponseEntity.ok(subscriptionService.getSubscribedEventsByInitiator(userId, initiatorId, from, size));
    }
}
