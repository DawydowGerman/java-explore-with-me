package ru.practicum.event.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import ru.practicum.event.model.Request;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.event.model.enums.Status;
import java.util.List;
import java.util.Optional;

public interface RequestJPARepository extends JpaRepository<Request, Long> {
    @Query(value = "SELECT * FROM requests WHERE id = :id AND requester = :requester", nativeQuery = true)
    Optional<Request> findByRequesterIdAndEventId(@Param("requester")Long requester, @Param("id")Long id);

    @Query(value = "SELECT COUNT(*) FROM requests " +
                   "WHERE event = :event", nativeQuery = true)
    int countByEvents(@Param("event") Long event);

    @Query(value = "SELECT * FROM requests WHERE event = :event", nativeQuery = true)
    Optional<List<Request>> findAllRequestsByEventId(@Param("event") Long event);

    @Query("SELECT COUNT(r) FROM Request r WHERE r.event.id = :eventId AND r.status = :status")
    long countByEventIdAndStatus(@Param("eventId") Long eventId, @Param("status") Status status);

    @Query(value = "SELECT * FROM requests WHERE requester = :requester", nativeQuery = true)
    Optional<List<Request>> getRequestsByUserId(@Param("requester")Long requester);

    @Modifying
    @Query(value = "UPDATE requests SET status = :status WHERE id = :id AND requester = :requester", nativeQuery = true)
    int cancelRequest(@Param("requester")Long requester, @Param("id")Long id, @Param("status") Status status);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM requests WHERE requester = :requesterId AND event = :eventId)",
            nativeQuery = true)
    boolean existsByEventIdAndRequesterId(@Param("requesterId") Long requesterId, @Param("eventId") Long eventId);
}