package ru.practicum.event.storage;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.event.model.Event;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventJPARepository extends JpaRepository<Event, Long> {
    @Query(value = "SELECT * FROM events WHERE initiator = :id", nativeQuery = true)
    Optional<List<Event>> findAllById(@Param("id") Long id);

    @Query(value = "SELECT * FROM events WHERE initiator = :id ORDER BY id OFFSET :from LIMIT :size", nativeQuery = true)
    Optional<List<Event>> findAllByIdWithPagination(@Param("id") Long id, @Param("from") int from, @Param("size") int size);

    @Query(value = "SELECT * FROM events WHERE initiator = :initiatorId AND id = :eventId", nativeQuery = true)
    Optional<Event> findByIdAndInitiatorId(@Param("eventId") Long eventId,@Param("initiatorId") Long initiatorId);

    @Query(value = "SELECT * FROM events " +
            "WHERE (CAST(:userIds as BIGINT) IS NULL OR initiator IN (:userIds)) " +
            "AND (CAST(:states as smallint) IS NULL OR state IN (:states)) " +
            "AND (CAST(:categoryIds as BIGINT) IS NULL OR category_id IN (:categoryIds)) " +
            "AND (CAST(:rangeStart AS timestamp) IS NULL OR event_date >= CAST(:rangeStart AS timestamp)) " +
            "AND (CAST(:rangeEnd AS timestamp) IS NULL OR event_date <= CAST(:rangeEnd AS timestamp)) " +
            "ORDER BY id DESC OFFSET :offset LIMIT :size", nativeQuery = true)
    Optional<List<Event>> findEventsByAdminFilters(
            @Param("userIds") List<Long> userIds,
            @Param("states") List<Integer> states,
            @Param("categoryIds") List<Long> categoryIds,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            @Param("offset") int offset,
            @Param("size") int size);

    @Query(value = "SELECT * FROM events " +
            "WHERE state = 1 AND " +
            "(:text IS NULL OR (annotation ILIKE CONCAT('%', :text, '%') OR description ILIKE CONCAT('%', :text, '%'))) " +
            "AND (CAST(:categoryIds as BIGINT) IS NULL OR category_id IN (:categoryIds)) " +
            "AND (CAST(:paid as BOOLEAN) IS NULL OR paid = CAST(:paid as BOOLEAN)) " +
            "AND ( " +
            "    (CAST(:rangeStart AS timestamp) IS NULL AND CAST(:rangeEnd AS timestamp) IS NULL AND event_date > CAST(:currentTimestamp AS timestamp)) " +
            "    OR  " +
            "    (CAST(:rangeStart AS timestamp) IS NOT NULL AND CAST(:rangeEnd AS timestamp) IS NOT NULL AND event_date BETWEEN CAST(:rangeStart AS timestamp) AND CAST(:rangeEnd AS timestamp)) " +
            ") " +
            "AND ((CAST(:onlyAvailable as BOOLEAN) IS NULL OR CAST(:onlyAvailable as BOOLEAN) is false) OR confirmed_requests < participant_limit) " +
            "ORDER BY " +
            "    CASE WHEN :sort = 'EVENT_DATE' THEN event_date END, " +
            "    CASE WHEN :sort = 'VIEWS' THEN views END " +
            "OFFSET :offset LIMIT :size", nativeQuery = true)
    Optional<List<Event>> getEventsPublicFilters(
            @Param("text") String text,
            @Param("categoryIds") List<Long> categoryIds,
            @Param("paid") Boolean paid,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            @Param("currentTimestamp") LocalDateTime currentTimestamp,
            @Param("onlyAvailable") Boolean onlyAvailable,
            @Param("sort") String sort,
            @Param("offset") int offset,
            @Param("size") int size);

    @Query(value = "SELECT * FROM events WHERE state = 1 AND id = :id", nativeQuery = true)
    Optional<Event> findByIdPublic(@Param("id") Long id);

    @Query(value = "SELECT * FROM events WHERE id IN (:ids)", nativeQuery = true)
    List<Event> findAllByIds(@Param("ids") List<Long> ids);

    @Modifying
    @Query(value = "UPDATE events SET confirmed_requests = " +
            "COALESCE(confirmed_requests, 0) + :incrementValue " +
            "WHERE id = :eventId",nativeQuery = true)
    void incrementConfirmedRequests(@Param("eventId") Long eventId, @Param("incrementValue") Integer incrementValue);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM events WHERE category_id = :id)", nativeQuery = true)
    boolean existsByCategory(@Param("id") Long id);

    @EntityGraph(attributePaths = {"initiator", "category", "location"})
    Optional<Event> findWithCreatorCategoryLocationById(Long id);

    @Modifying
    @Query(value = "UPDATE events SET views = views + 1 WHERE id = :id", nativeQuery = true)
    void incrementViewsNative(@Param("id") Long id);
}