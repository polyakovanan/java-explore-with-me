package ru.practicum.core.persistance.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

    @EmbeddedId
    private SubscriptionId id;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubscriptionId implements Serializable {

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "subscriber_id")
        private User subscriber;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "subscribed_id")
        private User subscribed;
    }
}
