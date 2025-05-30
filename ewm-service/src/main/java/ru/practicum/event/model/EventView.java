package ru.practicum.event.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.event.dto.EventViewId;

@Entity
@Table(name = "event_views")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@IdClass(EventViewId.class)
public class EventView {
    @Id
    @Column(name = "event_id")
    private Long eventId;

    @Id
    @Column(name = "ip")
    private String ip;

    @Column(name = "viewed_at")
    private LocalDateTime viewedAt;
}
