package ru.practicum.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.model.Hit;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HitJPARepository extends JpaRepository<Hit, Long> {
    @Query(value = "SELECT * " +
            "FROM hits " +
            "WHERE timestamp BETWEEN (:start) AND (:end) " +
            "ORDER BY timestamp", nativeQuery = true)
    List<Hit> findAllByTimestampBetween(@Param("start")LocalDateTime start, @Param("end")LocalDateTime end);

    @Query(value = "SELECT * " +
            "FROM hits " +
            "WHERE timestamp " +
            "BETWEEN (:start) AND (:end) " +
            "AND uri IN (:uris) " +
            "ORDER BY timestamp", nativeQuery = true)
    List<Hit> findAllByTimestampBetweenAndUriIn(@Param("start")LocalDateTime start, @Param("end")LocalDateTime end,
                                                @Param("uris")List<String> uris);

    @Query("SELECT COUNT(h) FROM Hit h WHERE h.uri = :uri AND h.ip = :ip " +
            "AND h.timestamp BETWEEN :start AND :end")
    long countByUriAndIpAndTimestampBetween(
            @Param("uri") String uri,
            @Param("ip") String ip,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(DISTINCT h.ip) FROM Hit h WHERE h.uri = :uri")
    long countUniqueIpsByUri(@Param("uri") String uri);

    @Query("SELECT COUNT(h) FROM Hit h WHERE h.uri = :uri")
    long countByUri(@Param("uri") String uri);
}