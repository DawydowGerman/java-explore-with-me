package ru.practicum.comment.service;

import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import java.util.List;

public interface CommentService {
    CommentDto addComment(Long eventId, Long userId, NewCommentDto newCommentDto);

    CommentDto addReply(Long eventId, Long parentId, Long userId, NewCommentDto newCommentDto);

    List<CommentDto> getTopLevelComments(Long eventId, int from, int size);

    List<CommentDto> getCommentsForEvent(Long eventId, int from, int size);

    List<CommentDto> getRepliesForComment(Long commentId, int from, int size);

    List<CommentDto> getCommentThread(Long commentId, int from, int size);

    List<CommentDto> getFilteredComments(String text,
                                         String rangeStartStr, String rangeEndStr,
                                         int from, int size);

    List<CommentDto> getCommentsByAuthor(Long userId, int from, int size);

    CommentDto updateComment(Long userId, Long commentId, NewCommentDto updateCommentDto);

    CommentDto updateAdminComment(Long userId, Long commentId, NewCommentDto updateCommentDto);

    void deleteComment(Long userId, Long commentId);

    void deleteAdminComment(Long userId, Long commentId);

    CommentDto getCommentByAuthorIdAndCommentId(Long userId, Long commentId);
}