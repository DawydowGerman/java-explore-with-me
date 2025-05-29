package ru.practicum.user.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.user.model.User;

import java.util.Collection;
import java.util.List;

@Repository
public interface UserJPARepository extends JpaRepository<User, Long> {
    @Query(value = "SELECT * FROM users WHERE id IN (:ids)", nativeQuery = true)
    List<User> findAllByIds(@Param("ids") List<Long> ids);

    @Query(value = "SELECT * FROM users ORDER BY id OFFSET :from LIMIT :size", nativeQuery = true)
    List<User> findAllWithPagination(@Param("from") int from, @Param("size") int size);

    long countByIdIn(Collection<Long> ids);
}