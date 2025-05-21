package ru.practicum.core.persistance.model.dto.request;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ParticipationRequestDto {
    private Long id;
    private Long event;
    private Long requester;
    private ParticipationRequestStatus status;
    private LocalDateTime created;
}
