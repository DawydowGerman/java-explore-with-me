package ru.practicum.event.service;

import ru.practicum.event.dto.statusupdaterequest.EventRequestStatusUpdateRequest;
import ru.practicum.event.dto.EventRequestStatusUpdateResult;
import ru.practicum.event.dto.ParticipationRequestDto;
import java.util.List;

public interface RequestService {
    ParticipationRequestDto addParticipationRequest(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateRequestStatus(
            Long userId, Long eventId,
            EventRequestStatusUpdateRequest statusUpdateRequest);

    List<ParticipationRequestDto> getRequestsByUserId(Long userId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);
}