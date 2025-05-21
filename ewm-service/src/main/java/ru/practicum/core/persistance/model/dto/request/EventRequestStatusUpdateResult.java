package ru.practicum.core.persistance.model.dto.request;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class EventRequestStatusUpdateResult {
    private Set<ParticipationRequestDto> confirmedRequests;
    private Set<ParticipationRequestDto> rejectedRequests;
}
