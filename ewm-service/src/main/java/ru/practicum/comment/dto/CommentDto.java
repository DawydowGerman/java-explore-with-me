package ru.practicum.comment.dto;
import lombok.*;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.user.dto.UserShortDto;
import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CommentDto {
    private Long id;
    private String text;
    private EventShortDto event;
    private UserShortDto author;
    private LocalDateTime createdAt;
    private String path;
}