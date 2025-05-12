package ru.practicum.server.service;

import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.StatsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {
    EndpointHitDto hit(EndpointHitDto endpointHit);

    List<StatsDto> get(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique);
}
