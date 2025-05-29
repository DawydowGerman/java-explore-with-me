package ru.practicum.category.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.category.model.Category;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryJPARepository extends JpaRepository<Category, Long> {
    @Query(value = "SELECT EXISTS(SELECT 1 FROM categories WHERE name = :name)", nativeQuery = true)
    boolean existsByName(@Param("name") String name);

    @Query(value = "SELECT * FROM categories WHERE name = :name", nativeQuery = true)
    Category findByName(@Param("name") String name);

    @Query(value = "SELECT * FROM categories ORDER BY id OFFSET :from LIMIT :size", nativeQuery = true)
    Optional<List<Category>> findAllByIdWithPagination(@Param("from") int from, @Param("size") int size);

    @Query(value = "SELECT * FROM categories WHERE id = :id", nativeQuery = true)
    Optional<Category> getCategoryById(@Param("id") Long id);

    long countByIdIn(Collection<Long> ids);
}