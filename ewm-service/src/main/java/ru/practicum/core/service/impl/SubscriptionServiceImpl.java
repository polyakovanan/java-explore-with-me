package ru.practicum.core.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.core.exception.ConditionsNotMetException;
import ru.practicum.core.exception.NotFoundException;
import ru.practicum.core.persistance.model.Subscription;
import ru.practicum.core.persistance.model.User;
import ru.practicum.core.persistance.model.dto.event.EventShortDto;
import ru.practicum.core.persistance.model.dto.user.UserShortDto;
import ru.practicum.core.persistance.model.mapper.EventMapper;
import ru.practicum.core.persistance.model.mapper.UserMapper;
import ru.practicum.core.persistance.repository.EventRepository;
import ru.practicum.core.persistance.repository.SubscriptionRepository;
import ru.practicum.core.persistance.repository.UserRepository;
import ru.practicum.core.service.SubscriptionService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public List<EventShortDto> getSubscribedEvents(Long userId, Integer from, Integer size) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        List<Subscription> subscriptions = subscriptionRepository.findAllByIdSubscriberId(userId);
        if (subscriptions.isEmpty()) {
            return List.of();
        }
        List<Long> initiatorsIds = subscriptions.stream().map(s -> s.getId().getSubscribed().getId()).toList();
        return eventRepository.findAllByInitiatorIdIn(initiatorsIds, from, size).stream().map(EventMapper::toEventShortDto).toList();
    }

    @Override
    public List<EventShortDto> getSubscribedEventsByInitiator(Long userId, Long initiatorId, Integer from, Integer size) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        User initiator = userRepository.findById(initiatorId).orElseThrow(() -> new NotFoundException("Пользователь с id " + initiatorId + " не найден"));
        subscriptionRepository.findById(new Subscription.SubscriptionId(user, initiator)).orElseThrow(() -> new ConditionsNotMetException("Пользователь с id " + userId + " не подписан на пользователя с id " + initiatorId));
        return eventRepository.findAllByInitiatorIdIn(List.of(initiatorId), from, size).stream().map(EventMapper::toEventShortDto).toList();
    }

    @Override
    public List<UserShortDto> getSubscribers(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        List<Subscription> subscribers = subscriptionRepository.findAllByIdSubscribedId(userId);
        return subscribers.stream().map(subscription -> subscription.getId().getSubscriber()).map(UserMapper::userToShortDto).toList();
    }

    @Override
    public List<UserShortDto> getSubscriptions(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        List<Subscription> subscriptions = subscriptionRepository.findAllByIdSubscriberId(userId);
        return subscriptions.stream().map(subscription -> subscription.getId().getSubscribed()).map(UserMapper::userToShortDto).toList();
    }

    @Override
    @Transactional
    public void subscribe(Long userId, Long initiatorId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        User initiator = userRepository.findById(initiatorId).orElseThrow(() -> new NotFoundException("Пользователь с id " + initiatorId + " не найден"));

        if (subscriptionRepository.findById(new Subscription.SubscriptionId(user, initiator)).isPresent()) {
            throw new ConditionsNotMetException("Пользователь уже подписан на данного пользователя");
        }
        subscriptionRepository.save(Subscription.builder()
                .id(new Subscription.SubscriptionId(user, initiator))
                .build());

        initiator.setSubscribers(initiator.getSubscribers() + 1);
        userRepository.save(initiator);
    }

    @Override
    @Transactional
    public void unsubscribe(Long userId, Long initiatorId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        User initiator = userRepository.findById(initiatorId).orElseThrow(() -> new NotFoundException("Пользователь с id " + initiatorId + " не найден"));

        Subscription subscription = subscriptionRepository.findById(new Subscription.SubscriptionId(user, initiator)).orElseThrow(() -> new NotFoundException("Пользователь не подписан на данного пользователя"));
        subscriptionRepository.delete(subscription);
        initiator.setSubscribers(initiator.getSubscribers() - 1);
        userRepository.save(initiator);
    }

    @Override
    @Transactional
    public void remove(Long userId, Long subscriberId) {
        User initiator = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        User user = userRepository.findById(subscriberId).orElseThrow(() -> new NotFoundException("Пользователь с id " + subscriberId + " не найден"));
        Subscription subscription = subscriptionRepository.findById(new Subscription.SubscriptionId(user, initiator)).orElseThrow(() -> new NotFoundException("Пользователь не подписан на данного пользователя"));
        subscriptionRepository.delete(subscription);
        initiator.setSubscribers(initiator.getSubscribers() - 1);
        userRepository.save(initiator);
    }
}
