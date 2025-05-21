package ru.practicum.core.persistance.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.core.persistance.model.dto.request.ParticipationRequestStatus;

import java.time.LocalDateTime;

@Entity(name = "requests")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    Event event;

    @ManyToOne
    User requester;

    @Column(nullable = false)
    LocalDateTime created;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    ParticipationRequestStatus status;
}
