package ru.practicum.event.dto;

import lombok.*;
import java.io.Serializable;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class EventViewId implements Serializable {
    private Long eventId;
    private String ip;
}