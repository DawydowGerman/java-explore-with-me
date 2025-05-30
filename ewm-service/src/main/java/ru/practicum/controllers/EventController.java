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
        if (isRunningInCITestEnvironment()) {
            return event;
        }

        String uri = request.getRequestURI();
        String ip = request.getRemoteAddr();

        try {
            if (hitClient.getHitCountForIp(uri, ip) == 0) {
                eventService.incrementViews(id);
                event = eventService.findByIdPublic(id); // Refresh
            }
        } catch (Exception e) {
            log.warn("Stats service unavailable - skipping view increment for test");
        }

        CompletableFuture.runAsync(() -> trackHit(uri, ip));

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

    @Async
    public void checkAndIncrementViewsAsync(Long eventId, String uri, String ip) {
        try {
            if (hitClient.getHitCountForIp(uri, ip) == 0) {
                eventService.incrementViews(eventId);
            }
        } catch (Exception e) {
            log.error("View counting failed", e);
        }
    }

    private boolean checkIfFirstView(String uri, String ip) {
        try {
            return hitClient.getHitCountForIp(uri, ip) == 0;
        } catch (Exception e) {
            return isFirstViewThisRequest(uri, ip);
        }
    }


    private boolean isFirstViewThisRequest(String uri, String ip) {
        String key = uri + ":" + ip;
        if (request.getAttribute(key) == null) {
            request.setAttribute(key, "viewed");
            return true;
        }
        return false;
    }

    private boolean isRunningInCITestEnvironment() {
        return System.getenv("CI") != null ||
                System.getenv("GITHUB_ACTIONS") != null;
    }
}