package ru.practicum.compilation.storage;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.compilation.model.Compilation;
import java.util.List;
import java.util.Optional;

public interface CompilationJPARepository extends JpaRepository<Compilation, Long> {
    @Query("SELECT c FROM Compilation c LEFT JOIN FETCH c.events ORDER BY c.id")
    Optional<List<Compilation>> findAllWithPagination(Pageable pageable);

    @Query("SELECT DISTINCT c FROM Compilation c LEFT JOIN FETCH c.events " +
            "WHERE (:pinned IS NULL OR c.pinned = :pinned) " +
            "ORDER BY c.id")
    Optional<List<Compilation>> findAllWithPaginationAndPinned(Pageable pageable,
            @Param("pinned") Boolean pinned);
}