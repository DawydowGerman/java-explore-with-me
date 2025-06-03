package ru.practicum.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.service.CommentService;
import ru.practicum.event.dto.*;
import ru.practicum.event.dto.ParticipationRequestDto;
import ru.practicum.event.dto.statusupdaterequest.EventRequestStatusUpdateRequest;
import ru.practicum.event.dto.updateeventuser.UpdateEventUserRequest;
import ru.practicum.event.service.EventService;
import ru.practicum.event.service.RequestService;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Validated
public class UsersController {
    private final EventService eventService;
    private final RequestService requestService;
    private final CommentService commentService;

    @PostMapping("/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto addEvent(@PathVariable Long userId,
                                 @RequestBody @Valid NewEventDto newEventDto) {
        return eventService.addEvent(userId, newEventDto);
    }

    @PostMapping("/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto addParticipationRequest(@PathVariable Long userId,
                                                           @RequestParam Long eventId) {
        return requestService.addParticipationRequest(userId, eventId);
    }

    @PostMapping("/{userId}/comments/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addComment(@PathVariable Long eventId,
                                 @PathVariable Long userId,
                                 @RequestBody @Valid NewCommentDto newCommentDto) {
        log.info("Creating comment for event {} by user {}", eventId, userId);
        return commentService.addComment(eventId, userId, newCommentDto);
    }

    @PostMapping("/{userId}/replies/{eventId}/{parentId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addReply(@PathVariable Long eventId,
                               @PathVariable Long userId,
                               @PathVariable Long parentId,
                               @RequestBody @Valid NewCommentDto newCommentDto) {
        log.info("Creating reply to comment {} by user {}", parentId, userId);
        return commentService.addReply(eventId, parentId, userId, newCommentDto);
    }

    @GetMapping("/top-level-comments/{eventId}")
    public List<CommentDto> getTopLevelComments(@PathVariable Long eventId,
                                                @RequestParam(defaultValue = "0") Integer from,
                                                @RequestParam(defaultValue = "10") Integer size) {
        log.info("Getting top-level comments for event {}", eventId);
        return commentService.getTopLevelComments(eventId, from, size);
    }

    @GetMapping("/all-event-comments/{eventId}")
    public List<CommentDto> getCommentsForEvent(@PathVariable Long eventId,
                                                @RequestParam(defaultValue = "0") Integer from,
                                                @RequestParam(defaultValue = "10") Integer size) {
        log.info("Getting all comments for event {}", eventId);
        return commentService.getCommentsForEvent(eventId, from, size);
    }

    @GetMapping("/replies/{commentId}")
    public List<CommentDto> getRepliesForComment(@PathVariable Long commentId,
                                                 @RequestParam(defaultValue = "0") Integer from,
                                                 @RequestParam(defaultValue = "10") Integer size) {
        log.info("Getting replies of comment {}", commentId);
        return commentService.getRepliesForComment(commentId, from, size);
    }

    @GetMapping("/thread/{commentId}")
    public List<CommentDto> getCommentThread(@PathVariable Long commentId,
                                             @RequestParam(defaultValue = "0") Integer from,
                                             @RequestParam(defaultValue = "10") Integer size) {
        log.info("Getting replies of comment {}", commentId);
        return commentService.getCommentThread(commentId, from, size);
    }

    @GetMapping("/filtered")
    public List<CommentDto> getFilteredComments(@RequestParam(required = false) String text,
                                                @RequestParam(required = false) String rangeStart,
                                                @RequestParam(required = false) String rangeEnd,
                                                @RequestParam(defaultValue = "0") Integer from,
                                                @RequestParam(defaultValue = "10") Integer size) {
        log.info("Getting filtered comments");
        return commentService.getFilteredComments(text, rangeStart, rangeEnd, from, size);
    }

    @GetMapping("/{userId}/comments")
    public List<CommentDto> getCommentsByAuthor(@PathVariable Long userId,
                                                @RequestParam(defaultValue = "0") Integer from,
                                                @RequestParam(defaultValue = "10") Integer size) {
        log.info("Getting comments by user {}", userId);
        return commentService.getCommentsByAuthor(userId, from, size);
    }

    @GetMapping("/{userId}/comments/{commentId}")
    public CommentDto getCommentsByAuthor(@PathVariable Long userId,
                                                @PathVariable Long commentId) {
        log.info("Getting comments by user {}", userId);
        return commentService.getCommentByAuthorIdAndCommentId(userId, commentId);
    }

    @GetMapping("/{userId}/events")
    public List<EventShortDto> getEventsById(@PathVariable(name = "userId") Long userId,
                                             @RequestParam(defaultValue = "0") Integer from,
                                             @RequestParam(defaultValue = "10") Integer size) {
        return eventService.getEventsById(userId, from, size);
    }

    @GetMapping("/{userId}/events/{eventId}")
    public EventFullDto getEventsById(@PathVariable(name = "userId") Long userId,
                                      @PathVariable(name = "eventId") Long eventId) {
        return eventService.getEventById(userId, eventId);
    }

    @GetMapping("/{userId}/requests")
    public List<ParticipationRequestDto> findAllRequestsByEventId(@PathVariable(name = "userId") Long userId) {
        return requestService.getRequestsByUserId(userId);
    }

    @GetMapping("/{userId}/events/{eventId}/requests")
    public List<ParticipationRequestDto> findAllRequestsByEventId(@PathVariable(name = "userId") Long userId,
                                                                  @PathVariable(name = "eventId") Long eventId) {
        return eventService.findAllRequestsByEventId(userId, eventId);
    }

    @PatchMapping("/{userId}/events/{eventId}")
    public EventFullDto updateEvent(@PathVariable Long userId,
                                    @PathVariable Long eventId,
                                    @Valid @RequestBody UpdateEventUserRequest updateEventUserRequest) {
        return eventService.updateEventByUser(userId, eventId, updateEventUserRequest);
    }

    @PatchMapping("/{userId}/events/{eventId}/requests")
    public EventRequestStatusUpdateResult updateRequestStatus(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody EventRequestStatusUpdateRequest request) {
        return requestService.updateRequestStatus(userId, eventId, request);
    }

    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PathVariable Long userId,
                                                 @PathVariable Long requestId) {
        return requestService.cancelRequest(userId, requestId);
    }

   @PatchMapping("/{userId}/comments/{commentId}")
    public CommentDto updateComment(@PathVariable Long userId,
                                    @PathVariable Long commentId,
                                    @RequestBody @Valid NewCommentDto updateCommentDto) {
        log.info("Updating comment {} by user {}", commentId, userId);
        return commentService.updateComment(userId, commentId, updateCommentDto);
    }

    @DeleteMapping("/{userId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long userId,
                              @PathVariable Long commentId) {
        log.info("Deleting comment {} by user {}", commentId, userId);
        commentService.deleteComment(userId, commentId);
    }
}