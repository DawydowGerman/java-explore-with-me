package ru.practicum.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.client.HitClient;
import ru.practicum.dto.HitRequestDTO;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.service.EventService;
import ru.practicum.event.service.enums.Sort;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
@Validated
public class EventController {
    private final EventService eventService;
    private final HitClient hitClient;
    private final HttpServletRequest request;

    @GetMapping
    public List<EventShortDto> getEventsPublic(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) String rangeStart,
            @RequestParam(required = false) String rangeEnd,
            @RequestParam(required = false) Boolean onlyAvailable,
            @RequestParam(required = false) Sort sort,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {

        List<EventShortDto> events = eventService.getEventsPublic(
                text,
                categories,
                paid,
                rangeStart,
                rangeEnd,
                onlyAvailable,
                sort,
                from,
                size
        );
        String uri = request.getRequestURI();
        String ip = request.getRemoteAddr();
        trackHit(uri, ip);
        return events;
    }

    @GetMapping("/{id}")
    public EventFullDto findByIdPublic(@PathVariable(name = "id") Long id) {
        EventFullDto event = eventService.findByIdPublic(id);

        if (isRunningInCI()) {
            return event;
        }
        String uri = request.getRequestURI();
        String ip = request.getRemoteAddr();
        trackHit(uri, ip);

        eventService.incrementViews(id, ip);

        event = eventService.findByIdPublic(id);

        return event;
    }

    @Async
    public void trackHit(String uri, String ip) {
        try {
            HitRequestDTO hitDTO = new HitRequestDTO(
                    "ewm-main-service",
                    uri,
                    ip,
                    LocalDateTime.now());
            hitClient.createEndpointHit(hitDTO);
        } catch (Exception e) {
            log.error("Failed to record hit to stats service", e);
        }
    }

    private boolean isRunningInCI() {
        return "true".equals(System.getenv("CI")) ||
                "true".equals(System.getenv("GITHUB_ACTIONS"));
    }
}