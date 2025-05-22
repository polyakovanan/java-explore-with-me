package ru.practicum.server.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.StatsDto;
import ru.practicum.server.service.StatsServiceImpl;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
public class StatsController {

    private final StatsServiceImpl service;

    @PostMapping("/hit")
    public ResponseEntity<EndpointHitDto> hit(@RequestBody EndpointHitDto endpointHit) {
        log.info("Получен запрос POST /hit");
        return new ResponseEntity<>(service.hit(endpointHit), HttpStatus.CREATED);
    }

    @GetMapping("/stats")
    public ResponseEntity<List<StatsDto>> getStats(@RequestParam()
                                           @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                                                   @RequestParam()
                                           @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
                                                   @RequestParam(required = false) List<String> uris,
                                                   @RequestParam(defaultValue = "false") Boolean unique) {
        log.info("Получен запрос GET /stats");
        return ResponseEntity.ok(service.get(start, end, uris, unique));
    }
}
