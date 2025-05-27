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
    private Long id;

    @Column(nullable = false)
    private String annotation;

    @ManyToOne
    private Category category;

    @Column(name = "confirmed_requests", nullable = false)
    private Long confirmedRequests = 0L;

    @Column(name = "created_on", nullable = false)
    private LocalDateTime createdOn;

    @Column(nullable = false)
    private String description;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    @ManyToOne
    private User initiator;

    @Column(nullable = false)
    private Double lat;

    @Column(nullable = false)
    private Double lon;

    @Column(nullable = false)
    private Boolean paid = false;

    @Column(name = "participant_limit", nullable = false)
    private Long participantLimit = 0L;

    @Column(name = "published_on")
    private LocalDateTime publishedOn;

    @Column(name = "request_moderation", nullable = false)
    private Boolean requestModeration = true;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EventState state;

    @Column(nullable = false)
    private String title;

    @Column
    private Long views;
}
