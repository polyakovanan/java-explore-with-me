package ru.practicum.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.StatsDto;
import ru.practicum.server.mapper.EndpointHitMapper;
import ru.practicum.server.mapper.StatsMapper;
import ru.practicum.server.repository.EndpointHitsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {
    private final EndpointHitsRepository endpointHitsRepository;

    public EndpointHitDto hit(EndpointHitDto endpointHit) {
        return EndpointHitMapper.toEndpointHitDto(
                endpointHitsRepository.save(EndpointHitMapper.toEndpointHit(endpointHit))
        );
    }

    public List<StatsDto> get(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Дата начала не может быть позже даты окончания");
        }
        if (Boolean.TRUE.equals(unique)) {
            return endpointHitsRepository.findUniqueStats(start, end, uris)
                    .stream()
                    .map(StatsMapper::toStatsDto)
                    .toList();
        } else {
            return endpointHitsRepository.findStats(start, end, uris)
                    .stream()
                    .map(StatsMapper::toStatsDto)
                    .toList();
        }
    }
}
