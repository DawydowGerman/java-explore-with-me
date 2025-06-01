package ru.practicum.event.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class LocationDTO {
    private Long id;
    private Float lat;
    private Float lon;

    public LocationDTO(Float lat, Float lon) {
        this.lat = lat;
        this.lon = lon;
    }
}