package ru.practicum.compilation.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.mapper.EventMapper;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CompilationMapper {
    public static Compilation toModelFromNew(NewCompilationDto newCompilationDto) {
        return new Compilation(
                newCompilationDto.getPinned(),
                newCompilationDto.getTitle()
        );
    }

    public static Compilation toModelFromDTO(CompilationDto compilationDto) {
        return new Compilation(
                compilationDto.getId(),
                compilationDto.getPinned(),
                compilationDto.getTitle()
        );
    }

    public static CompilationDto toDTO(Compilation compilation) {
        List<EventShortDto> eventShortDtos = compilation.getEvents().stream()
                .map(EventMapper::toShortDto)
                .collect(Collectors.toList());
        return new CompilationDto(
                compilation.getId(),
                compilation.getPinned(),
                compilation.getTitle(),
                eventShortDtos
        );
    }
}