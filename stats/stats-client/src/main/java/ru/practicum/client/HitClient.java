package ru.practicum.client;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.dto.HitRequestDTO;
import jakarta.validation.Valid;
import ru.practicum.dto.StatsDto;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class HitClient {
    private final RestTemplate restTemplate;
    private final String serverUrl;

    public HitClient(
            RestTemplate restTemplate,
            @Value("${hit.service.url:http://localhost:9090}") String serverUrl) {
        this.restTemplate = restTemplate;
        this.serverUrl = serverUrl;
    }

    public void createEndpointHit(@Valid HitRequestDTO hitDTO) {
        String url = serverUrl + "/hit";
        restTemplate.postForEntity(url, hitDTO, Void.class);
    }

    public List<StatsDto> getStats(LocalDateTime start, LocalDateTime end,
                                   List<String> uris, boolean unique) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serverUrl + "/stats")
                .queryParam("start", start)
                .queryParam("end", end)
                .queryParam("unique", unique);

        if (uris != null && !uris.isEmpty()) {
            builder.queryParam("uris", uris.toArray());
        }

        ResponseEntity<StatsDto[]> response = restTemplate.getForEntity(
                builder.toUriString(),
                StatsDto[].class);

        return List.of(response.getBody());
    }

    public long getHitCountForIp(String uri, String ip) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serverUrl + "/stats/ip")
                .queryParam("start", LocalDateTime.now().minusYears(1))
                .queryParam("end", LocalDateTime.now())
                .queryParam("uri", uri)
                .queryParam("ip", ip);
        ResponseEntity<Long> response = restTemplate.getForEntity(
                builder.toUriString(),
                Long.class);
        return response.getBody() != null ? response.getBody() : 0L;
    }

    public long getTotalViewsForUri(String uri) {
        return restTemplate.getForObject(
                serverUrl + "/stats/total?uri={uri}",
                Long.class,
                uri
        );
    }
}