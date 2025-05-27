package ru.practicum.core.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.core.exception.ConditionsNotMetException;
import ru.practicum.core.exception.NotFoundException;
import ru.practicum.core.persistance.model.Event;
import ru.practicum.core.persistance.model.ParticipationRequest;
import ru.practicum.core.persistance.model.User;
import ru.practicum.core.persistance.model.dto.event.state.EventState;
import ru.practicum.core.persistance.model.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.core.persistance.model.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.core.persistance.model.dto.request.ParticipationRequestDto;
import ru.practicum.core.persistance.model.dto.request.ParticipationRequestStatus;
import ru.practicum.core.persistance.model.mapper.ParticipationRequestMapper;
import ru.practicum.core.persistance.repository.EventRepository;
import ru.practicum.core.persistance.repository.ParticipationRequestRepository;
import ru.practicum.core.persistance.repository.UserRepository;
import ru.practicum.core.service.ParticipationRequestService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParticipationRequestServiceImpl implements ParticipationRequestService {
    private final ParticipationRequestRepository participationRequestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public List<ParticipationRequestDto> getAllByUser(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        return participationRequestRepository.findAllByRequesterId(userId)
                .stream()
                .map(ParticipationRequestMapper::toParticipationRequestDto).toList();
    }

    @Override
    public List<ParticipationRequestDto> getAllByEventAndInitiator(Long userId, Long eventId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConditionsNotMetException("Заявки на участие в событии может просмотреть только создатель события");
        }

        return participationRequestRepository.findAllByEventId(eventId)
                .stream()
                .map(ParticipationRequestMapper::toParticipationRequestDto)
                .toList();
    }

    @Override
    @Transactional
    public ParticipationRequestDto create(Long userId, Long eventId) {
        User requester = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));
        if (event.getInitiator().getId().equals(userId)) {
            throw new ConditionsNotMetException("Нельзя заявить участие в собственном событии");
        }
        if (event.getState() != EventState.PUBLISHED) {
            throw new ConditionsNotMetException("Нельзя заявить участие в неопубликованном событии");
        }
        if (!participationRequestRepository.findAllByEventIdAndRequesterId(eventId, userId).isEmpty()) {
            throw new ConditionsNotMetException("Нельзя отправить дублирующую заявку на участие в событии");
        }
        if (event.getParticipantLimit() != 0 && Objects.equals(event.getConfirmedRequests(), event.getParticipantLimit())) {
            throw new ConditionsNotMetException("Достигнут лимит заявок на участие в событии");
        }

        ParticipationRequest participationRequest = ParticipationRequest.builder()
                .requester(requester)
                .event(event)
                .status(event.getParticipantLimit() > 0 && event.getRequestModeration() ? ParticipationRequestStatus.PENDING : ParticipationRequestStatus.CONFIRMED)
                .created(LocalDateTime.now())
                .build();
        if (!event.getRequestModeration()) {
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
        }

        return ParticipationRequestMapper.toParticipationRequestDto(participationRequestRepository.save(participationRequest));
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancel(Long userId, Long requestId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        ParticipationRequest participationRequest = participationRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Заявка с id=" + requestId + " не найдена"));
        if (!participationRequest.getRequester().getId().equals(userId)) {
            throw new ConditionsNotMetException("Заявку на участие в событии можно отменить только пользователем, который её отправил");
        }

        Event event = eventRepository.findById(participationRequest.getEvent().getId())
                .orElseThrow(() -> new NotFoundException("Событие с id=" + participationRequest.getEvent().getId() + " не найдено"));

        event.setConfirmedRequests(event.getConfirmedRequests() - 1);
        eventRepository.save(event);

        participationRequest.setStatus(ParticipationRequestStatus.CANCELED);

        participationRequestRepository.save(participationRequest);
        return ParticipationRequestMapper.toParticipationRequestDto(participationRequest);
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest requestDto) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConditionsNotMetException("Заявки на участие в событии может обновить только создатель события");
        }
        if (event.getParticipantLimit() == 0) {
            throw new ConditionsNotMetException("Нельзя обновить статус заявок на участие в событии с отключенной модерацией заявок");
        }

        List<ParticipationRequest> participationRequests = participationRequestRepository.findAllByEventId(eventId);
        List<Long> requestIds = participationRequests.stream().map(ParticipationRequest::getId).toList();
        List<Long> absentRequestIds = new ArrayList<>();
        requestDto.getRequestIds().forEach(id -> {
            if (!requestIds.contains(id)) {
                absentRequestIds.add(id);
            }
        });

        if (!absentRequestIds.isEmpty()) {
            throw new NotFoundException("Заявки на участие с id=" + absentRequestIds + " не найдены");
        }

        List<ParticipationRequest> participationRequestsToUpdate = participationRequests.stream()
                .filter(participationRequest -> requestDto.getRequestIds().contains(participationRequest.getId()))
                .toList();

        List<Long> notPendingRequests = participationRequestsToUpdate.stream()
                .filter(participationRequest -> participationRequest.getStatus() != ParticipationRequestStatus.PENDING)
                .map(ParticipationRequest::getId)
                .toList();

        if (!notPendingRequests.isEmpty()) {
            throw new ConditionsNotMetException("Заявки на участие в событии с id=" + eventId + " не находятся в состоянии ожидания подтверждения");
        }

        if (requestDto.getStatus() == ParticipationRequestStatus.CONFIRMED) {
            if (event.getConfirmedRequests() + requestDto.getRequestIds().size() > event.getParticipantLimit()) {
                throw new ConditionsNotMetException("Нельзя подтвердить заявки на участие в событии, так как превышен лимит заявок");
            }

            participationRequestsToUpdate.forEach(participationRequest -> participationRequest.setStatus(ParticipationRequestStatus.CONFIRMED));
            participationRequestRepository.saveAll(participationRequests);
            event.setConfirmedRequests(event.getConfirmedRequests() + requestDto.getRequestIds().size());
            eventRepository.save(event);

            if (Objects.equals(event.getConfirmedRequests(), event.getParticipantLimit())) {
                List<ParticipationRequest> participationRequestsForDeny = participationRequests.stream()
                        .filter(participationRequest -> !requestDto.getRequestIds().contains(participationRequest.getId()))
                        .toList();

                participationRequestsForDeny
                        .forEach(participationRequest -> participationRequest.setStatus(ParticipationRequestStatus.REJECTED));

                participationRequestRepository.saveAll(participationRequestsForDeny);
            }
        } else if (requestDto.getStatus() == ParticipationRequestStatus.REJECTED) {
            participationRequestsToUpdate.forEach(participationRequest -> participationRequest.setStatus(ParticipationRequestStatus.REJECTED));
            participationRequestRepository.saveAll(participationRequests);
            event.setConfirmedRequests(event.getConfirmedRequests() - requestDto.getRequestIds().size());
            eventRepository.save(event);
        }

        participationRequests = participationRequestRepository.findAllByEventId(eventId);
        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(participationRequests
                        .stream()
                        .filter(participationRequest -> participationRequest.getStatus() == ParticipationRequestStatus.CONFIRMED)
                        .map(ParticipationRequestMapper::toParticipationRequestDto)
                        .collect(Collectors.toSet()))
                .rejectedRequests(participationRequests
                        .stream()
                        .filter(participationRequest -> participationRequest.getStatus() == ParticipationRequestStatus.REJECTED)
                        .map(ParticipationRequestMapper::toParticipationRequestDto)
                        .collect(Collectors.toSet()))
                .build();
    }
}
