package ru.practicum.server.mapper;

import ru.practicum.dto.EndpointHitDto;
import ru.practicum.server.model.EndpointHit;
import ru.practicum.utils.SimpleDateTimeFormatter;

import java.time.LocalDateTime;

public class EndpointHitMapper {
    private EndpointHitMapper() {

    }

    public static EndpointHit toEndpointHit(EndpointHitDto endpointHitDto) {
        return EndpointHit.builder()
                .app(endpointHitDto.getApp())
                .uri(endpointHitDto.getUri())
                .ip(endpointHitDto.getIp())
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static EndpointHitDto toEndpointHitDto(EndpointHit endpointHit) {
        return EndpointHitDto.builder()
                .app(endpointHit.getApp())
                .uri(endpointHit.getUri())
                .ip(endpointHit.getIp())
                .timestamp(SimpleDateTimeFormatter.toString(endpointHit.getTimestamp()))
                .build();
    }
}
