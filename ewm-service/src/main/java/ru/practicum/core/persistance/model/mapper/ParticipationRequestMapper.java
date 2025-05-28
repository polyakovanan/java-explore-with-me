package ru.practicum.core.persistance.model.mapper;

import ru.practicum.core.persistance.model.ParticipationRequest;
import ru.practicum.core.persistance.model.dto.request.ParticipationRequestDto;

public class ParticipationRequestMapper {
    private ParticipationRequestMapper() {

    }

    public static ParticipationRequestDto toParticipationRequestDto(ParticipationRequest participationRequest) {
        return ParticipationRequestDto.builder()
                .id(participationRequest.getId())
                .requester(participationRequest.getRequester().getId())
                .event(participationRequest.getEvent().getId())
                .status(participationRequest.getStatus())
                .created(participationRequest.getCreated())
                .build();
    }
}
