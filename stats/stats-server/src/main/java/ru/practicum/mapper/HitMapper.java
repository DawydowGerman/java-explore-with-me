package ru.practicum.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.dto.HitRequestDTO;
import ru.practicum.dto.StatsDto;
import ru.practicum.model.Hit;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HitMapper {
    public static Hit toModel(HitRequestDTO hitDTO) {
        return new Hit(
                hitDTO.getApp(),
                hitDTO.getUri(),
                hitDTO.getIp(),
                hitDTO.getTimestamp()
        );
    }

    public static StatsDto toStatsDto(Hit hit) {
        return new StatsDto(
                hit.getApp(),
                hit.getUri()
        );
    }
}