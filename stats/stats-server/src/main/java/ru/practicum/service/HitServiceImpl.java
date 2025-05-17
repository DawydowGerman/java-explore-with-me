package ru.practicum.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.dto.HitRequestDTO;
import ru.practicum.dto.StatsDto;
import ru.practicum.mapper.HitMapper;
import ru.practicum.model.Hit;
import ru.practicum.storage.HitJPARepository;
import ru.practicum.exception.ValidationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class HitServiceImpl implements HitService {
    private final HitJPARepository hitJPARepository;

    @Autowired
    public HitServiceImpl(HitJPARepository hitJPARepository) {
        this.hitJPARepository = hitJPARepository;
    }

    @Transactional
    public void createEndpointHit(HitRequestDTO hitDTO) {
        Hit hit = HitMapper.toModel(hitDTO);
        hit = hitJPARepository.save(hit);
    }

    public List<StatsDto> getStats(LocalDateTime start, LocalDateTime end,
                                   List<String> uris, boolean unique) {
        if (start == null || end == null) {
            throw new ValidationException("Start and end dates are required");
        }

        if (start.isAfter(end)) {
            throw new ValidationException("Start date must be before end date");
        }

        List<Hit> hits;
        if (uris == null || uris.isEmpty()) {
            hits = hitJPARepository.findAllByTimestampBetween(start, end);
        } else {
            hits = hitJPARepository.findAllByTimestampBetweenAndUriIn(start, end, uris);
        }

        List<StatsDto> statsList;
        if (unique == false) {
            statsList = hits.stream()
                    .collect(Collectors.groupingBy(
                            Hit::getUri,
                            Collectors.counting()
                    )).entrySet()
                    .stream()
                    .map(entry -> new StatsDto(
                            hits.get(0).getApp(),
                            entry.getKey(),
                            entry.getValue()
                    ))
                    .collect(Collectors.toList());
            statsList.sort((o1, o2) -> Long.compare(o2.getHits(), o1.getHits()));
            return statsList;
        }

        List<Hit> distinctList = new ArrayList<>(
                hits.stream()
                        .collect(Collectors.toMap(
                                Hit::getIp,
                                Function.identity(),
                                (existing, replacement) -> existing
                        ))
                        .values()
        );
        statsList = distinctList.stream()
                .collect(Collectors.groupingBy(
                        Hit::getUri,
                        Collectors.counting()
                )).entrySet()
                .stream()
                .map(entry -> new StatsDto(
                        hits.get(0).getApp(),
                        entry.getKey(),
                        entry.getValue()
                ))
                .collect(Collectors.toList());
        statsList.sort((o1, o2) -> Long.compare(o2.getHits(), o1.getHits()));
        return statsList;
    }
}