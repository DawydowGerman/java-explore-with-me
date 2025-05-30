package ru.practicum.event.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.category.storage.CategoryJPARepository;
import ru.practicum.event.dto.*;
import ru.practicum.event.dto.ParticipationRequestDto;
import ru.practicum.event.dto.updateeventadmin.StateAction;
import ru.practicum.event.dto.updateeventadmin.UpdateEventAdminRequest;
import ru.practicum.event.dto.updateeventuser.StateActionUser;
import ru.practicum.event.dto.updateeventuser.UpdateEventUserRequest;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.mapper.LocationMapper;
import ru.practicum.event.mapper.RequestMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.Location;
import ru.practicum.event.model.Request;
import ru.practicum.event.model.enums.State;
import ru.practicum.event.service.enums.Sort;
import ru.practicum.event.storage.EventJPARepository;
import ru.practicum.event.storage.LocationJPARepository;
import ru.practicum.event.storage.RequestJPARepository;
import ru.practicum.exception.ConstraintException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.user.model.User;
import ru.practicum.user.storage.UserJPARepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EventServiceImpl implements EventService {
    private final EventJPARepository eventJPARepository;
    private final LocationJPARepository locationJPARepository;
    private final UserJPARepository userJPARepository;
    private final CategoryJPARepository categoryJPARepository;
    private final RequestJPARepository requestJPARepository;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public EventServiceImpl(EventJPARepository eventJPARepository,
                            LocationJPARepository locationJPARepository,
                            UserJPARepository userJPARepository, CategoryJPARepository categoryJPARepository,
                            RequestJPARepository requestJPARepository) {
        this.eventJPARepository = eventJPARepository;
        this.locationJPARepository = locationJPARepository;
        this.userJPARepository = userJPARepository;
        this.categoryJPARepository = categoryJPARepository;
        this.requestJPARepository = requestJPARepository;
    }

    @Transactional
    public EventFullDto addEvent(Long userId, NewEventDto newEventDto) {
        if (!userJPARepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }
        if (!categoryJPARepository.existsById(newEventDto.getCategory())) {
            throw new NotFoundException("Category with id=" + newEventDto.getCategory() + " was not found");
        }
        Location location = LocationMapper.toModel(newEventDto.getLocation());
        locationJPARepository.save(location);

        Event event = EventMapper.toModel(newEventDto);
        event.setCategory(categoryJPARepository.getCategoryById(newEventDto.getCategory()).get());
        event.setLocation(location);
        event.setCreatedOn(LocalDateTime.now());
        event.setState(State.PENDING);
        User initiator = userJPARepository.findById(userId).get();
        event.setInitiator(initiator);
        event = eventJPARepository.save(event);
        EventFullDto result = EventMapper.toFullDto(event);
        return result;
    }

    public List<EventShortDto> getEventsById(Long userId, Integer from, Integer size) {
        User requester = userJPARepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
        List<Event> events;
        if (from == null && size == null) {
            if (eventJPARepository.findAllById(userId).isPresent()) {
                events = eventJPARepository.findAllById(userId).get();
            } else return Collections.emptyList();
        } else {
            if (eventJPARepository.findAllByIdWithPagination(userId, from, size).isPresent()) {
                events = eventJPARepository.findAllByIdWithPagination(userId, from, size).get();
            } else return Collections.emptyList();
        }
        return events.stream()
                .map(EventMapper::toShortDto)
                .collect(Collectors.toList());
    }

    public EventFullDto getEventById(Long userId, Long eventId) {
        User requester = userJPARepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
        Event event = eventJPARepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
        return EventMapper.toFullDto(event);
    }

    public List<ParticipationRequestDto> findAllRequestsByEventId(Long userId, Long eventId) {
        List<Request> requests;
        User requester = userJPARepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
        Event event = eventJPARepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
        if (requestJPARepository.findAllRequestsByEventId(eventId).isEmpty()) {
            return Collections.emptyList();
        }
        requests = requestJPARepository.findAllRequestsByEventId(eventId).get();
        return requests.stream()
                .map(RequestMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<EventFullDto> getEventsAdmin(
            List<Long> userIds,
            List<String> states,
            List<Long> categoryIds,
            String rangeStartStr,
            String rangeEndStr,
            Integer from,
            Integer size) {
        if (userIds != null && !userIds.isEmpty()) {
            validateUsersExist(userIds);
        }

        List<Integer> statesList = new ArrayList<>();
        if (states != null) {
            statesList = states.stream()
                    .map(str -> State.valueOf(str))
                    .map(state -> {
                        switch (state) {
                            case PENDING:
                                return 0;
                            case PUBLISHED:
                                return 1;
                            case CANCELED:
                                return 2;
                            default:
                                throw new IllegalArgumentException("Unknown state: " + state);
                        }
                    })
                    .collect(Collectors.toList());
        }
        LocalDateTime rangeStart = parseDateTime(rangeStartStr);
        LocalDateTime rangeEnd = parseDateTime(rangeEndStr);
        validateRanges(rangeStart, rangeEnd);
        if (categoryIds != null && !categoryIds.isEmpty()) {
            validateCategoriesExist(categoryIds);
        }
        int offset = from != null ? from : 0;
        int limit = size != null ? size : 10;
        List<Event> result = eventJPARepository.findEventsByAdminFilters(
                userIds,
                statesList,
                categoryIds,
                rangeStart,
                rangeEnd,
                offset,
                limit
        ).orElse(Collections.emptyList());
        return result.stream()
                .map(EventMapper::toFullDto)
                .collect(Collectors.toList());
    }

    public List<EventShortDto> getEventsPublic(
            String text,
            List<Long> categories,
            Boolean paid,
            String rangeStartStr,
            String rangeEndStr,
            Boolean onlyAvailable,
            Sort sort,
            Integer from,
            Integer size) {
        LocalDateTime rangeStart = parseDateTime(rangeStartStr);
        LocalDateTime rangeEnd = parseDateTime(rangeEndStr);

        validateRanges(rangeStart, rangeEnd);

        if (categories != null && !categories.isEmpty()) {
            validateCategoriesExist(categories);
        }

        String sortStr = "EVENT_DATE";
        if (sort == Sort.EVENT_DATE) {
            sortStr = "EVENT_DATE";
        }  else if (sort == Sort.VIEWS) {
            sortStr = "VIEWS";
        }
        int offset = from != null ? from : 0;
        int limit = size != null ? size : 10;
        List<Event> result = eventJPARepository.getEventsPublicFilters(
                text,
                categories,
                paid,
                rangeStart,
                rangeEnd,
                LocalDateTime.now(),
                onlyAvailable,
                sortStr,
                offset,
                limit
        ).orElse(Collections.emptyList());

        return result.stream()
                .map(EventMapper::toShortDto)
                .collect(Collectors.toList());
    }

    public EventFullDto findByIdPublic(Long id) {
        Event event = eventJPARepository.findByIdAndState(id, State.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        return EventMapper.toFullDto(event);
    }

    @Transactional
    public EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateRequest) {
        userJPARepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
        Event event = eventJPARepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
        if (event.getState() == State.PUBLISHED) {
            throw new ConstraintException("Only pending or canceled events can be changed");
        }
        if (updateRequest.getAnnotation() != null) {
            event.setAnnotation(updateRequest.getAnnotation());
        }
        if (updateRequest.getCategory() != null) {
            event.setCategory(categoryJPARepository.findById(updateRequest.getCategory()).get());
        }
        if (updateRequest.getDescription() != null) {
            event.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getEventDate() != null) {
            event.setEventDate(updateRequest.getEventDate());
        }
        if (updateRequest.getLocation() != null) {
            Location location = LocationMapper.toModel(updateRequest.getLocation());
            location = locationJPARepository.save(location);
            event.setLocation(location);
        }
        if (updateRequest.getPaid() != null) {
            event.setPaid(updateRequest.getPaid());
        }
        if (updateRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateRequest.getParticipantLimit());
        }
        if (updateRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateRequest.getRequestModeration());
        }
        if (updateRequest.getTitle() != null) {
            event.setTitle(updateRequest.getTitle());
        }
        if (event.getState() == State.CANCELED) {
            event.setState(State.PENDING);
        }
        if (updateRequest.getStateAction() == StateActionUser.CANCEL_REVIEW) {
            event.setState(State.CANCELED);
        }
        Event updatedEvent = eventJPARepository.save(event);
        return EventMapper.toFullDto(updatedEvent);
    }

   @Transactional
   public EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest updateRequest) {
       Event event = eventJPARepository.findById(eventId)
               .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
       if (updateRequest.getStateAction() == StateAction.PUBLISH_EVENT) {
           if (updateRequest.getEventDate() != null &&
                   updateRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
               throw new ConstraintException("Cannot publish the event because event date is less than 1 hour from now");
           }
           if (event.getState() != State.PENDING) {
               throw new ConstraintException("Cannot publish the event because it's not in the right state: " + event.getState());
           }
       }
       if (updateRequest.getStateAction() == StateAction.REJECT_EVENT) {
           if (event.getState() == State.PUBLISHED) {
               throw new ConstraintException("Cannot reject the event because it's already published");
           }
       }
       if (updateRequest.getAnnotation() != null) {
           event.setAnnotation(updateRequest.getAnnotation());
       }
       if (updateRequest.getCategory() != null) {
           event.setCategory(categoryJPARepository.findById(updateRequest.getCategory()).get());
       }
       if (updateRequest.getDescription() != null) {
           event.setDescription(updateRequest.getDescription());
       }
       if (updateRequest.getEventDate() != null) {
           if (updateRequest.getEventDate().isBefore(LocalDateTime.now())) {
               throw new ValidationException("eventDate cannot be in the past");
           }
           event.setEventDate(updateRequest.getEventDate());
       }
       if (updateRequest.getLocation() != null) {
           Location location = LocationMapper.toModel(updateRequest.getLocation());
           location = locationJPARepository.save(location);
           event.setLocation(location);
       }
       if (updateRequest.getPaid() != null) {
           event.setPaid(updateRequest.getPaid());
       }
       if (updateRequest.getParticipantLimit() != null) {
           event.setParticipantLimit(updateRequest.getParticipantLimit());
       }
       if (updateRequest.getRequestModeration() != null) {
           event.setRequestModeration(updateRequest.getRequestModeration());
       }
       if (updateRequest.getTitle() != null) {
           event.setTitle(updateRequest.getTitle());
       }
       if (updateRequest.getStateAction() != null) {
           switch (updateRequest.getStateAction()) {
               case PUBLISH_EVENT:
                   event.setState(State.PUBLISHED);
                   event.setPublishedOn(LocalDateTime.now());
                   break;
               case REJECT_EVENT:
                   event.setState(State.CANCELED);
                   break;
           }
       }
       event = eventJPARepository.save(event);
       EventFullDto result = EventMapper.toFullDto(event);
       return result;
   }

    public void incrementViews(Long eventId) {
        eventJPARepository.incrementViews(eventId);
    }

    private State parseState(String stateName) {
        try {
            return State.valueOf(stateName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid state value: " + stateName);
        }
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null) {
            return null;
        }
        dateTimeStr = dateTimeStr.replace("%20", " ");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(dateTimeStr, formatter);
    }

    private void validateCategoriesExist(Collection<Long> categoryIds) {
        if (categoryJPARepository.countByIdIn(categoryIds) != categoryIds.size()) {
            throw new NotFoundException("One or more categories not found");
        }
    }

    private void validateUsersExist(Collection<Long> userIds) {
        if (userJPARepository.countByIdIn(userIds) != userIds.size()) {
            throw new NotFoundException("One or more users not found");
        }
    }

    private void validateRanges(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("rangeStart must be before rangeEnd");
        }
    }
}