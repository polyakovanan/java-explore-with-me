package ru.practicum.core.service;

import ru.practicum.core.persistance.model.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.core.persistance.model.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.core.persistance.model.dto.request.ParticipationRequestDto;

import java.util.List;

public interface ParticipationRequestService {
    List<ParticipationRequestDto> getAllByUser(Long userId);

    List<ParticipationRequestDto> getAllByEventAndInitiator(Long userId, Long eventId);

    ParticipationRequestDto create(Long userId, Long eventId);

    ParticipationRequestDto cancel(Long userId, Long requestId);

    EventRequestStatusUpdateResult updateStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest eventDto);
}
