package ru.practicum.core.persistance.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.core.persistance.model.dto.event.state.EventState;

import java.time.LocalDateTime;

@Entity(name = "events")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String annotation;

    @ManyToOne
    Category category;

    @Column(name = "confirmed_requests", nullable = false)
    Long confirmedRequests = 0L;

    @Column(name = "created_on", nullable = false)
    LocalDateTime createdOn;

    @Column(nullable = false)
    String description;

    @Column(name = "event_date", nullable = false)
    LocalDateTime eventDate;

    @ManyToOne
    User initiator;

    @Column(nullable = false)
    Double lat;

    @Column(nullable = false)
    Double lon;

    @Column(nullable = false)
    Boolean paid = false;

    @Column(name = "participant_limit", nullable = false)
    Long participantLimit = 0L;

    @Column(name = "published_on")
    LocalDateTime publishedOn;

    @Column(name = "request_moderation", nullable = false)
    Boolean requestModeration = true;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    EventState state;

    @Column(nullable = false)
    String title;

    @Column
    Long views;
}
