package ru.practicum.comment.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.comment.model.Comment;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.query.Param;

public interface CommentJPARepository extends JpaRepository<Comment, Long> {
    @Query(value = "SELECT * FROM comments WHERE event_id = :eventId AND path ~ :pathPattern OFFSET :offset LIMIT :size",
            nativeQuery = true)
    Optional<List<Comment>> findByEventIdAndPathLike(@Param("eventId") Long eventId,
                                                     @Param("pathPattern") String pathPattern,
                                                     @Param("offset") int offset,
                                                     @Param("size") int size);

    @Query(value = "SELECT * FROM comments WHERE event_id = :eventId OFFSET :offset LIMIT :size", nativeQuery = true)
    Optional<List<Comment>> getCommentsForEvent(@Param("eventId") Long eventId, @Param("offset") int offset, @Param("size") int size);

    @Query(value = "SELECT * FROM comments WHERE path LIKE CONCAT(:commentId, '/%') OFFSET :offset LIMIT :size",
            nativeQuery = true)
    Optional<List<Comment>> getRepliesForComment(@Param("commentId") Long commentId, @Param("offset") int offset, @Param("size") int size);

    @Query(value = "SELECT * FROM comments " +
            "WHERE path LIKE CONCAT(:commentId, '/%') " +
            "   OR id = :commentId " +
            "ORDER BY path OFFSET :offset LIMIT :size",
            nativeQuery = true)
    Optional<List<Comment>> getCommentThread(@Param("commentId") Long commentId, @Param("offset") int offset, @Param("size") int size);

    @Query(value = "SELECT * FROM comments " +
            "WHERE " +
            "(:text IS NULL OR text ILIKE CONCAT('%', :text, '%')) " +
            "AND (CAST(:rangeStart AS timestamp) IS NULL OR created >= CAST(:rangeStart AS timestamp)) " +
            "AND (CAST(:rangeEnd AS timestamp) IS NULL OR created <= CAST(:rangeEnd AS timestamp)) " +
            "OFFSET :offset LIMIT :size",
            nativeQuery = true)
    Optional<List<Comment>> findWithFilters(@Param("text") String text,
                                  @Param("rangeStart") LocalDateTime rangeStart,
                                  @Param("rangeEnd") LocalDateTime rangeEnd,
                                  @Param("offset") int offset,
                                  @Param("size") int size);

    @Query(value = "SELECT * FROM comments WHERE author_id = :authorId OFFSET :offset LIMIT :size", nativeQuery = true)
    Optional<List<Comment>> findByAuthorId(@Param("authorId") Long authorId, @Param("offset") int offset, @Param("size") int size);

    @Query(value = "SELECT * FROM comments WHERE author_id = :authorId AND id = :id", nativeQuery = true)
    Optional<Comment> getCommentByAuthorIdAndCommentId(@Param("authorId") Long authorId, @Param("id") Long id);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM comments WHERE author_id = :authorId AND id = :id)", nativeQuery = true)
    boolean existsByAuthorIdAndId(@Param("authorId") Long author_id, @Param("id") Long id);
}