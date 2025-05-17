package ru.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class StatsDto {
    private String app;
    private String uri;
    private Long hits;

    public StatsDto(String app, String uri) {
        this.app = app;
        this.uri = uri;
    }
}