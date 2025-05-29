package ru.practicum.event.service;

import ru.practicum.event.dto.*;
import ru.practicum.event.dto.ParticipationRequestDto;
import ru.practicum.event.dto.updateeventadmin.UpdateEventAdminRequest;
import ru.practicum.event.dto.updateeventuser.UpdateEventUserRequest;
import ru.practicum.event.service.enums.Sort;
import java.util.List;

public interface EventService {
    EventFullDto addEvent(Long userId, NewEventDto newEventDto);

    List<EventShortDto> getEventsById(Long id, Integer from, Integer size);

    EventFullDto getEventById(Long userId, Long eventId);

    List<ParticipationRequestDto> findAllRequestsByEventId(Long userId, Long eventId);

    EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateRequest);

    List<EventFullDto> getEventsAdmin(
            List<Long> userIds,
            List<String> stateNames,
            List<Long> categoryIds,
            String rangeStartStr,
            String rangeEndStr,
            Integer from,
            Integer size);

    List<EventShortDto> getEventsPublic(
            String text,
            List<Long> categories,
            Boolean paid,
            String rangeStart,
            String rangeEnd,
            Boolean onlyAvailable,
            Sort sort,
            Integer from,
            Integer size);

    EventFullDto findByIdPublic(Long id);

    EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest updateRequest);

    void incrementViews(Long eventId);
}