package ru.practicum.core.persistance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.core.persistance.model.Subscription;

import java.util.List;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Subscription.SubscriptionId> {

    List<Subscription> findAllByIdSubscriberId(Long userId);

    List<Subscription> findAllByIdSubscribedId(Long userId);
}
