package ru.practicum.core.persistance.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.core.persistance.model.Event;
import ru.practicum.core.persistance.model.dto.event.filter.EventSearchAdmin;
import ru.practicum.core.persistance.model.dto.event.filter.EventSearchCommon;
import ru.practicum.core.persistance.model.dto.event.state.EventState;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    @Query("SELECT e FROM events e " +
            "WHERE (:text IS NULL OR LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text, '%')) " +
            "OR LOWER(e.description) LIKE LOWER(CONCAT('%', :text, '%')) " +
            "OR LOWER(e.title) LIKE LOWER(CONCAT('%', :text, '%'))) " +
            "AND (:paid IS NULL OR e.paid = :paid) " +
            "AND (:categories IS NULL OR e.category.id IN :categories) " +
            "AND ((:rangeStart IS NULL AND :rangeEnd IS NULL AND e.eventDate > :currentTime) " +
            "OR (:rangeStart IS NOT NULL AND e.eventDate >= :rangeStart) " +
            "OR (:rangeEnd IS NOT NULL AND e.eventDate <= :rangeEnd)) " +
            "AND (:onlyAvailable IS NULL OR " +
            "     (:onlyAvailable = TRUE AND (e.participantLimit = 0 OR e.participantLimit > e.confirmedRequests)) " +
            "     OR :onlyAvailable = FALSE) " +
            "AND e.state = :state " +
            "ORDER BY " +
            "CASE WHEN :sort = 'EVENT_DATE' THEN e.eventDate END ASC, " +
            "CASE WHEN :sort = 'VIEWS' THEN e.views END DESC")
    List<Event> findCommonEventsByFilters(
            @Param("text") String text,
            @Param("paid") Boolean paid,
            @Param("categories") List<Long> categories,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            @Param("onlyAvailable") Boolean onlyAvailable,
            @Param("sort") String sort,
            @Param("state") EventState state,
            @Param("currentTime") LocalDateTime currentTime,
            Pageable pageable);

    default List<Event> findCommonEventsByFilters(EventSearchCommon eventSearchCommon) {
        Pageable pageable = Pageable.unpaged();
        Integer from = eventSearchCommon.getFrom();
        Integer size = eventSearchCommon.getSize();
        if (from != null && size != null) {
            pageable = Pageable.ofSize(size).withPage(from / size);
        }
        return findCommonEventsByFilters(
                eventSearchCommon.getText(),
                eventSearchCommon.getPaid(),
                eventSearchCommon.getCategories(),
                eventSearchCommon.getRangeStart(),
                eventSearchCommon.getRangeEnd(),
                eventSearchCommon.getOnlyAvailable(),
                eventSearchCommon.getSort().name(),
                EventState.PUBLISHED,
                LocalDateTime.now(),
                pageable);
    }

    @Query("SELECT e FROM events e " +
            "WHERE (:users IS NULL OR e.initiator.id IN :users) " +
            "AND (:states IS NULL OR e.state IN :states) " +
            "AND (:categories IS NULL OR e.category.id IN :categories) " +
            "AND ((COALESCE(:rangeStart, NULL) IS NULL OR e.eventDate >= :rangeStart) " +
            "AND ((COALESCE(:rangeEnd, NULL) IS NULL OR e.eventDate <= :rangeEnd) " +
            "ORDER BY e.eventDate DESC")
    List<Event> findAdminEventsByFilters(
            @Param("users") List<Long> users,
            @Param("states") List<String> states,
            @Param("categories") List<Long> categories,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            Pageable pageable);

    default List<Event> findAdminEventsByFilters(EventSearchAdmin eventSearchAdmin) {
        Pageable pageable = Pageable.unpaged();
        Integer from = eventSearchAdmin.getFrom();
        Integer size = eventSearchAdmin.getSize();
        if (from != null && size != null) {
            pageable = Pageable.ofSize(size).withPage(from / size);
        }
        return findAdminEventsByFilters(
                eventSearchAdmin.getUsers(),
                eventSearchAdmin.getStates(),
                eventSearchAdmin.getCategories(),
                eventSearchAdmin.getRangeStart(),
                eventSearchAdmin.getRangeEnd(),
                pageable
        );
    }

    @Query("SELECT e FROM events e " +
            "WHERE e.initiator.id = :user " +
            "ORDER BY e.eventDate DESC")
    List<Event> findAllByInitiatorId(@Param("user")Long userId, Pageable pageable);

    default List<Event> findAllByInitiatorId(Long userId, Integer from, Integer size) {
        if (from != null && size != null) {
            return findAllByInitiatorId(userId, Pageable.ofSize(size).withPage(from / size));
        }
        return findAllByInitiatorId(userId, Pageable.unpaged());
    }

    List<Event> findAllByCategoryId(Long categoryId);

    List<Event> findAllByIdIn(List<Long> list);
}
