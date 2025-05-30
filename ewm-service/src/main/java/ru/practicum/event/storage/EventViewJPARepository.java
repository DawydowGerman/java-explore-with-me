package ru.practicum.event.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.event.dto.EventViewId;
import ru.practicum.event.model.EventView;

@Repository
public interface EventViewJPARepository extends JpaRepository<EventView, EventViewId> {
    boolean existsByEventIdAndIp(Long eventId, String ip);
}
