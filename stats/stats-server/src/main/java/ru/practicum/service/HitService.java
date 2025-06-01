package ru.practicum.service;

import ru.practicum.dto.HitRequestDTO;
import ru.practicum.dto.StatsDto;
import java.time.LocalDateTime;
import java.util.List;

public interface HitService {
    void createEndpointHit(HitRequestDTO hitDTO);

    List<StatsDto> getStats(LocalDateTime start, LocalDateTime end,
                            List<String> uris, boolean unique);

    long getHitCountForIp(LocalDateTime start, LocalDateTime end, String uri, String ip);

    long countByUri(String uri);
}