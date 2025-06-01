package ru.practicum.compilation.service;

import org.springframework.data.domain.PageRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.storage.CompilationJPARepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.storage.EventJPARepository;
import ru.practicum.exception.NotFoundException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CompilationServiceImpl implements CompilationService {
    private final CompilationJPARepository compilationJPARepository;
    private final EventJPARepository eventJPARepository;

    @Autowired
    public CompilationServiceImpl(CompilationJPARepository compilationJPARepository,
                                 EventJPARepository eventJPARepository) {
        this.compilationJPARepository = compilationJPARepository;
        this.eventJPARepository = eventJPARepository;
    }

    @Transactional
    public CompilationDto saveCompilation(NewCompilationDto newCompilationDto) {
        Compilation compilation = CompilationMapper.toModelFromNew(newCompilationDto);
        if (newCompilationDto.getEvents() != null) {
            List<Event> events = eventJPARepository.findAllByIds(newCompilationDto.getEvents());
            compilation.setEvents(new HashSet<>(events));
        }
        if (newCompilationDto.getPinned() == null) {
            compilation.setPinned(false);
        }
        compilation = compilationJPARepository.save(compilation);
        CompilationDto result = CompilationMapper.toDTO(compilation);
        return result;
    }

    @Transactional
    public CompilationDto updateCompilation(Long compId, NewCompilationDto newCompilationDto) {
        Compilation compilation = compilationJPARepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(" Compilation with id=" + compId + " was not found"));
        if (newCompilationDto.getPinned() != null) {
            compilation.setPinned(newCompilationDto.getPinned());
        }
        if (newCompilationDto.getTitle() != null) {
            compilation.setTitle(newCompilationDto.getTitle());
        }
        if (newCompilationDto.getEvents() != null) {
            List<Event> eventList = eventJPARepository.findAllByIds(newCompilationDto.getEvents());
            compilation.setEvents(new HashSet<>(eventList));
        }
        compilation = compilationJPARepository.save(compilation);

        CompilationDto result = CompilationMapper.toDTO(compilation);
        return result;
    }

    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        List<Compilation> compilationList;
        if (pinned == null) {
            if (compilationJPARepository.findAllWithPagination(PageRequest.of(from / size, size)).isEmpty()) {
                return Collections.emptyList();
            }
            compilationList = compilationJPARepository.findAllWithPagination(PageRequest.of(from / size, size)).get();
        } else {
            if (compilationJPARepository
                    .findAllWithPaginationAndPinned(PageRequest.of(from / size, size), pinned).isEmpty()) {
                return Collections.emptyList();
            }
            compilationList = compilationJPARepository
                    .findAllWithPaginationAndPinned(PageRequest.of(from / size, size), pinned).get();
        }
        return compilationList.stream()
                .map(CompilationMapper::toDTO)
                .collect(Collectors.toList());
    }

     public CompilationDto getCompilation(Long compId) {
        if (compilationJPARepository.findById(compId).isEmpty()) {
            throw new NotFoundException("Compilation with id=" + compId + " was not found");
        }
         Compilation compilation = compilationJPARepository.findById(compId).get();
         return CompilationMapper.toDTO(compilation);
     }

    @Transactional
    public void removeCompilation(Long id) {
        if (!compilationJPARepository.existsById(id)) {
            throw new NotFoundException("User with id = " + id + " not found.");
        }
        compilationJPARepository.deleteById(id);
    }
}