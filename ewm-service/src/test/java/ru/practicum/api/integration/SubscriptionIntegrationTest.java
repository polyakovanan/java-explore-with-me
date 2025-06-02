package ru.practicum.api.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import ru.practicum.core.persistance.model.*;
import ru.practicum.core.persistance.model.dto.event.state.EventState;
import ru.practicum.core.persistance.repository.*;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SubscriptionIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    private MockMvc mockMvc;

    private User user1;
    private User user2;
    private Event event1;
    private Event event2;
    private Category category;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        // Создаем тестовых пользователей
        user1 = User.builder()
                .name("User 1")
                .email("user1@test.com")
                .subscribers(0L)
                .build();

        user2 = User.builder()
                .name("User 2")
                .email("user2@test.com")
                .subscribers(0L)
                .build();

        user1 = userRepository.save(user1);
        user2 = userRepository.save(user2);

        // Создаем категорию для событий
        category = Category.builder()
                .name("Test Category")
                .build();
        categoryRepository.save(category);

        // Создаем тестовые события
        event1 = Event.builder()
                .title("Event 1")
                .annotation("Annotation 1")
                .description("Description 1")
                .category(category)
                .initiator(user2)
                .eventDate(LocalDateTime.now().plusDays(1))
                .createdOn(LocalDateTime.now())
                .state(EventState.PUBLISHED)
                .lat(55.754167)
                .lon(37.62)
                .paid(false)
                .participantLimit(0L)
                .requestModeration(true)
                .build();

        event2 = Event.builder()
                .title("Event 2")
                .annotation("Annotation 2")
                .description("Description 2")
                .category(category)
                .initiator(user2)
                .eventDate(LocalDateTime.now().plusDays(2))
                .createdOn(LocalDateTime.now())
                .state(EventState.PUBLISHED)
                .lat(55.754167)
                .lon(37.62)
                .paid(false)
                .participantLimit(0L)
                .requestModeration(true)
                .build();

        eventRepository.save(event1);
        eventRepository.save(event2);
    }

    @Test
    void subscribeShouldCreateSubscription() throws Exception {
        mockMvc.perform(post("/users/{userId}/subscriptions/{initiatorId}", user1.getId(), user2.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        // Проверяем, что подписка создалась
        mockMvc.perform(get("/users/{userId}/subscriptions", user1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(user2.getId().intValue())));
    }

    @Test
    void getSubscriptionsShouldReturnSubscriptions() throws Exception {
        // Создаем подписку через репозиторий
        Subscription subscription = new Subscription();
        subscription.setId(new Subscription.SubscriptionId(user1, user2));
        subscriptionRepository.save(subscription);

        mockMvc.perform(get("/users/{userId}/subscriptions", user1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(user2.getId().intValue())))
                .andExpect(jsonPath("$[0].name", is(user2.getName())));
    }

    @Test
    void getSubscribersShouldReturnSubscribers() throws Exception {
        // Создаем подписку (user2 подписан на user1)
        Subscription subscription = new Subscription();
        subscription.setId(new Subscription.SubscriptionId(user2, user1));
        subscriptionRepository.save(subscription);

        mockMvc.perform(get("/users/{userId}/subscriptions/subscribers", user1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(user2.getId().intValue())))
                .andExpect(jsonPath("$[0].name", is(user2.getName())));
    }

    @Test
    void unsubscribeShouldRemoveSubscription() throws Exception {
        // Создаем подписку
        Subscription subscription = new Subscription();
        subscription.setId(new Subscription.SubscriptionId(user1, user2));
        subscriptionRepository.save(subscription);

        // Отписываемся
        mockMvc.perform(patch("/users/{userId}/subscriptions/{initiatorId}/cancel", user1.getId(), user2.getId()))
                .andExpect(status().isNoContent());

        // Проверяем, что подписок нет
        mockMvc.perform(get("/users/{userId}/subscriptions", user1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void removeShouldRemoveSubscriber() throws Exception {
        // Создаем подписку (user2 подписан на user1)
        Subscription subscription = new Subscription();
        subscription.setId(new Subscription.SubscriptionId(user2, user1));
        subscriptionRepository.save(subscription);

        // Удаляем подписчика
        mockMvc.perform(patch("/users/{userId}/subscriptions/{subscriberId}/remove", user1.getId(), user2.getId()))
                .andExpect(status().isNoContent());

        // Проверяем, что подписчиков нет
        mockMvc.perform(get("/users/{userId}/subscriptions/subscribers", user1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getSubscriptionEventsShouldReturnEvents() throws Exception {
        // Создаем подписку
        Subscription subscription = new Subscription();
        subscription.setId(new Subscription.SubscriptionId(user1, user2));
        subscriptionRepository.save(subscription);

        mockMvc.perform(get("/users/{userId}/subscriptions/events", user1.getId())
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is(event2.getTitle())))
                .andExpect(jsonPath("$[1].title", is(event1.getTitle())));
    }

    @Test
    void getSubscriptionEventsByInitiatorShouldReturnEvents() throws Exception {
        // Создаем подписку
        Subscription subscription = new Subscription();
        subscription.setId(new Subscription.SubscriptionId(user1, user2));
        subscriptionRepository.save(subscription);

        mockMvc.perform(get("/users/{userId}/subscriptions/events/{initiatorId}", user1.getId(), user2.getId())
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is(event2.getTitle())))
                .andExpect(jsonPath("$[1].title", is(event1.getTitle())));
    }

    @Test
    void getSubscriptionEventsShouldReturnEmptyListWhenNoSubscriptions() throws Exception {
        mockMvc.perform(get("/users/{userId}/subscriptions/events", user1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}