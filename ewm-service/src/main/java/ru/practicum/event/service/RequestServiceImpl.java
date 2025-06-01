package ru.practicum.event.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.event.dto.statusupdaterequest.EventRequestStatusUpdateRequest;
import ru.practicum.event.dto.EventRequestStatusUpdateResult;
import ru.practicum.event.dto.ParticipationRequestDto;
import ru.practicum.event.dto.statusupdaterequest.StatusUpdate;
import ru.practicum.event.mapper.RequestMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.Request;
import ru.practicum.event.model.enums.State;
import ru.practicum.event.model.enums.Status;
import ru.practicum.event.storage.EventJPARepository;
import ru.practicum.event.storage.RequestJPARepository;
import ru.practicum.exception.ConstraintException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.model.User;
import ru.practicum.user.storage.UserJPARepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RequestServiceImpl implements RequestService {
    private final EventJPARepository eventJPARepository;
    private final UserJPARepository userJPARepository;
    private final RequestJPARepository requestJPARepository;

    @Autowired
    public RequestServiceImpl(EventJPARepository eventJPARepository,
                              UserJPARepository userJPARepository, RequestJPARepository requestJPARepository) {
        this.eventJPARepository = eventJPARepository;
        this.userJPARepository = userJPARepository;
        this.requestJPARepository = requestJPARepository;
    }

    @Transactional
    public ParticipationRequestDto addParticipationRequest(Long userId, Long eventId) {
        User requester = userJPARepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
        Event event = eventJPARepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
        if (requestJPARepository.existsByEventIdAndRequesterId(userId, eventId)) {
            throw new ConstraintException("Cannot create a request with the same requesters");
        }
        if (!event.getState().equals(State.PUBLISHED)) {
            throw new ConstraintException("Cannot participate in unpublished event");
        }
        if (requestJPARepository.findByRequesterIdAndEventId(userId, eventId).isPresent()) {
            throw new ConstraintException("Participation request already exists");
        }
        if (event.getInitiator().getId().equals(userId)) {
            throw new ConstraintException("Initiator cannot add request to participate in their own event");
        }
        long confirmedRequests = requestJPARepository.countByEventIdAndStatus(eventId, Status.CONFIRMED);
        if (event.getParticipantLimit() > 0 && confirmedRequests >= event.getParticipantLimit()) {
            throw new ConstraintException("The participant limit has been reached");
        }
        Request request = new Request();
        request.setCreated(LocalDateTime.now());
        request.setEvent(event);
        request.setRequester(requester);
        if (event.getRequestModeration() == false || event.getParticipantLimit() == 0) {
            request.setStatus(Status.CONFIRMED);
        } else {
            request.setStatus(Status.PENDING);
        }
        request = requestJPARepository.save(request);
        return RequestMapper.toDto(request);
    }

    public List<ParticipationRequestDto> getRequestsByUserId(Long userId) {
        userJPARepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
        if (requestJPARepository.getRequestsByUserId(userId).isEmpty()) {
            return Collections.emptyList();
        }
        return requestJPARepository.getRequestsByUserId(userId).get()
                .stream()
                .map(RequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatus(
            Long userId, Long eventId,
            EventRequestStatusUpdateRequest statusUpdateRequest) {
        userJPARepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
        Event event = eventJPARepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            throw new ConstraintException("Confirmation of requests is not required for this event");
        }
        List<Request> allRequestedRequests = requestJPARepository.findAllById(statusUpdateRequest.getRequestIds());
        if (allRequestedRequests.size() != statusUpdateRequest.getRequestIds().size()) {
            Set<Long> foundIds = allRequestedRequests.stream()
                    .map(Request::getId)
                    .collect(Collectors.toSet());

            List<Long> missingIds = statusUpdateRequest.getRequestIds().stream()
                    .filter(id -> !foundIds.contains(id))
                    .collect(Collectors.toList());
            throw new NotFoundException("Some requests not found: " + missingIds);
        }
        List<Long> invalidStatusRequests = allRequestedRequests.stream()
                .filter(request -> request.getStatus() != Status.PENDING)
                .map(Request::getId)
                .collect(Collectors.toList());
        if (!invalidStatusRequests.isEmpty()) {
            throw new ConstraintException("Requests must have PENDING status. Invalid requests: " + invalidStatusRequests);
        }
        long confirmedRequests = requestJPARepository.countByEventIdAndStatus(eventId, Status.CONFIRMED);
        if (confirmedRequests >= event.getParticipantLimit()) {
            throw new ConstraintException("The participant limit has been reached");
        }
        StatusUpdate newStatus = statusUpdateRequest.getStatus();
        List<Request> confirmedRequestsList = new ArrayList<>();
        List<Request> rejectedRequestsList = new ArrayList<>();
        if (newStatus == StatusUpdate.CONFIRMED) {
            int availableSlots = (int) (event.getParticipantLimit() - confirmedRequests);
            int requestsToConfirm = Math.min(availableSlots, allRequestedRequests.size());
            for (int i = 0; i < requestsToConfirm; i++) {
                Request request = allRequestedRequests.get(i);
                request.setStatus(Status.CONFIRMED);
                confirmedRequestsList.add(request);
            }
            if (!confirmedRequestsList.isEmpty()) {
                eventJPARepository.incrementConfirmedRequests(
                        eventId,
                        confirmedRequestsList.size()
                );
            }
            if (requestsToConfirm < allRequestedRequests.size()) {
                for (int i = requestsToConfirm; i < allRequestedRequests.size(); i++) {
                    Request request = allRequestedRequests.get(i);
                    request.setStatus(Status.REJECTED);
                    rejectedRequestsList.add(request);
                }
            }
        } else {
            allRequestedRequests.forEach(request -> request.setStatus(Status.REJECTED));
            rejectedRequestsList.addAll(allRequestedRequests);
        }
        requestJPARepository.saveAll(allRequestedRequests);
        return new EventRequestStatusUpdateResult(
                confirmedRequestsList.stream().map(RequestMapper::toDto).collect(Collectors.toList()),
                rejectedRequestsList.stream().map(RequestMapper::toDto).collect(Collectors.toList())
        );
    }

    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        userJPARepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
        Request request = requestJPARepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request with id=" + requestId + " was not found"));
        request.setStatus(Status.CANCELED);
        requestJPARepository.save(request);
        return RequestMapper.toDto(request);
    }
}