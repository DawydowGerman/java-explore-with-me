package ru.practicum.comment.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.model.Comment;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.user.mapper.UserMapper;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentMapper {
    public static Comment toModel(NewCommentDto newCommentDto) {
        return new Comment(
                newCommentDto.getText()
        );
    }

    public static CommentDto toDto(Comment comment) {
        return new CommentDto(
                comment.getId(),
                comment.getText(),
                EventMapper.toShortDto(comment.getEvent()),
                UserMapper.toShortDto(comment.getAuthor()),
                comment.getCreatedAt(),
                comment.getPath()
                );
    }
}