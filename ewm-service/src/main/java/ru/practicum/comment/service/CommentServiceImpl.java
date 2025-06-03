package ru.practicum.comment.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.storage.CommentJPARepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.storage.EventJPARepository;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.user.model.User;
import ru.practicum.user.storage.UserJPARepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {
    private final CommentJPARepository commentJPARepository;
    private final EventJPARepository eventJPARepository;
    private final UserJPARepository userJPARepository;

    @Autowired
    public CommentServiceImpl(CommentJPARepository commentJPARepository, EventJPARepository eventJPARepository,
                              UserJPARepository userJPARepository) {
        this.commentJPARepository = commentJPARepository;
        this.eventJPARepository = eventJPARepository;
        this.userJPARepository = userJPARepository;
    }

    @Transactional
    public CommentDto addComment(Long eventId, Long userId, NewCommentDto newCommentDto) {
        Event event = eventJPARepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
        User commentAuthor = userJPARepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));

        Comment comment = CommentMapper.toModel(newCommentDto);
        comment.setEvent(event);
        comment.setAuthor(commentAuthor);
        comment.setCreatedAt(LocalDateTime.now());

        Comment savedComment = commentJPARepository.save(comment);
        savedComment.setPath(savedComment.getId().toString());
        Comment finalComment = commentJPARepository.save(savedComment);

        return CommentMapper.toDto(finalComment);
    }

    @Transactional
    public CommentDto addReply(Long eventId, Long parentId, Long userId, NewCommentDto newCommentDto) {
        Event event = eventJPARepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
        User commentAuthor = userJPARepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
        Comment parentComment = commentJPARepository.findById(parentId)
                .orElseThrow(() -> new NotFoundException("Parent comment not found"));

        Comment reply = CommentMapper.toModel(newCommentDto);
        reply.setEvent(event);
        reply.setAuthor(commentAuthor);
        reply.setCreatedAt(LocalDateTime.now());

        Comment savedReply = commentJPARepository.save(reply);
        savedReply.setPath(parentComment.getPath() + "/" + savedReply.getId());
        Comment finalReply = commentJPARepository.save(savedReply);

        return CommentMapper.toDto(finalReply);
    }

    public List<CommentDto> getTopLevelComments(Long eventId, int from, int size) {
        List<Comment> result = commentJPARepository
                .findByEventIdAndPathLike(eventId, "^[0-9]+$", from, size)
                .orElse(Collections.emptyList());
        return result
                .stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<CommentDto> getCommentsForEvent(Long eventId, int from, int size) {
        List<Comment> result = commentJPARepository
                .getCommentsForEvent(eventId, from, size)
                .orElse(Collections.emptyList());
        return result
                .stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<CommentDto> getRepliesForComment(Long commentId, int from, int size) {
        List<Comment> result = commentJPARepository
                .getRepliesForComment(commentId, from, size)
                .orElse(Collections.emptyList());
        return result
                .stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<CommentDto> getCommentThread(Long commentId, int from, int size) {
        List<Comment> result = commentJPARepository
                .getCommentThread(commentId, from, size)
                .orElse(Collections.emptyList());
        return result
                .stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<CommentDto> getFilteredComments(String text,
                                                String rangeStartStr, String rangeEndStr,
                                                int from, int size) {
        LocalDateTime rangeStart = parseDateTime(rangeStartStr);
        LocalDateTime rangeEnd = parseDateTime(rangeEndStr);

        List<Comment> result = commentJPARepository
                .findWithFilters(text, rangeStart, rangeEnd, from, size)
                .orElse(Collections.emptyList());
        return result
                .stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<CommentDto> getCommentsByAuthor(Long userId, int from, int size) {
        List<Comment> result = commentJPARepository
                .findByAuthorId(userId, from, size)
                .orElse(Collections.emptyList());
        return result
                .stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList());
    }

    public CommentDto getCommentByAuthorIdAndCommentId(Long userId, Long commentId) {
        Comment comment = commentJPARepository.getCommentByAuthorIdAndCommentId(userId, commentId)
                .orElseThrow(() -> new NotFoundException("Comment with id=" + commentId + " was not found"));
        return CommentMapper.toDto(comment);
    }

    @Transactional
    public CommentDto updateComment(Long userId, Long commentId, NewCommentDto updateCommentDto) {
        User commentAuthor = userJPARepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
        Comment comment = commentJPARepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with id=" + commentId + " was not found"));
        if (!commentJPARepository.existsByAuthorIdAndId(userId, commentId)) {
            throw new ValidationException("User with id: " + userId + " is not an author of comment with id " + commentId);
        }
        comment.setText(updateCommentDto.getText());
        Comment updatedComment = commentJPARepository.save(comment);
        return CommentMapper.toDto(updatedComment);
    }

    @Transactional
    public CommentDto updateAdminComment(Long userId, Long commentId, NewCommentDto updateCommentDto) {
        User commentAuthor = userJPARepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
        Comment comment = commentJPARepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with id=" + commentId + " was not found"));
        comment.setText(updateCommentDto.getText());
        Comment updatedComment = commentJPARepository.save(comment);
        return CommentMapper.toDto(updatedComment);
    }

    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = commentJPARepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with id=" + commentId + " was not found"));
        if (!commentJPARepository.existsByAuthorIdAndId(userId, commentId)) {
            throw new ValidationException("User with id: " + userId + " is not an author of comment with id " + commentId);
        }
        commentJPARepository.delete(comment);
    }

    @Transactional
    public void deleteAdminComment(Long userId, Long commentId) {
        Comment comment = commentJPARepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with id=" + commentId + " was not found"));
        commentJPARepository.delete(comment);
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null) {
            return null;
        }
        dateTimeStr = dateTimeStr.replace("%20", " ");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(dateTimeStr, formatter);
    }
}