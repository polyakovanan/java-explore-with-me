package ru.practicum.core.persistance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.core.persistance.model.ParticipationRequest;

import java.util.List;

@Repository
public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {
    List<ParticipationRequest> findAllByRequesterId(Long userId);

    List<ParticipationRequest> findAllByEventIdAndRequesterId(Long eventId, Long userId);

    List<ParticipationRequest> findAllByEventId(Long eventId);
}
