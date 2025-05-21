package ru.practicum.core.persistance.model.dto.request;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class EventRequestStatusUpdateRequest {
    private Set<Long> requestIds;
    private ParticipationRequestStatus status;
}
