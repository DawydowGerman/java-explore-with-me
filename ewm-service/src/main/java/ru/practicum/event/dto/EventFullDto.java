package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.event.model.enums.State;
import ru.practicum.user.dto.UserShortDto;
import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class EventFullDto {
    private String annotation;
    private CategoryDto category;
    private Long confirmedRequests;
    private LocalDateTime createdOn;
    private String description;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;
    private Long id;
    private UserShortDto initiator;
    private LocationDTO location;
    private Boolean paid;
    private Integer participantLimit = 0;
    private String publishedOn;
    private Boolean requestModeration = true;
    private State state;
    private String title;
    private Long views;

    public EventFullDto(String annotation, CategoryDto category, Long confirmedRequests,
                        LocalDateTime createdOn, String description, LocalDateTime eventDate,
                        Long id, UserShortDto initiator, LocationDTO location, Boolean paid,
                        Integer participantLimit, Boolean requestModeration, State state,
                        String title, Long views) {
        this.annotation = annotation;
        this.category = category;
        this.confirmedRequests = confirmedRequests;
        this.createdOn = createdOn;
        this.description = description;
        this.eventDate = eventDate;
        this.id = id;
        this.initiator = initiator;
        this.location = location;
        this.paid = paid;
        this.participantLimit = participantLimit;
        this.requestModeration = requestModeration;
        this.state = state;
        this.title = title;
        this.views = views;
    }
}