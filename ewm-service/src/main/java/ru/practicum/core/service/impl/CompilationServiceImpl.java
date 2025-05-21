package ru.practicum.core.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.core.exception.ConditionsNotMetException;
import ru.practicum.core.exception.NotFoundException;
import ru.practicum.core.persistance.model.Compilation;
import ru.practicum.core.persistance.model.Event;
import ru.practicum.core.persistance.model.dto.compilation.CompilationDto;
import ru.practicum.core.persistance.model.dto.compilation.NewCompilationDto;
import ru.practicum.core.persistance.model.dto.compilation.UpdateCompilationRequest;
import ru.practicum.core.persistance.model.dto.event.state.EventState;
import ru.practicum.core.persistance.model.mapper.CompilationMapper;
import ru.practicum.core.persistance.repository.CompilationRepository;
import ru.practicum.core.persistance.repository.EventRepository;
import ru.practicum.core.service.CompilationService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service("compilationService")
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Override
    public List<CompilationDto> findAll(Boolean pinned, Integer from, Integer size) {
        return compilationRepository.findCompilations(pinned, from, size).stream().map(CompilationMapper::toCompilationDto).toList();
    }

    @Override
    public CompilationDto findById(Long compId) {
        return CompilationMapper.toCompilationDto(
                compilationRepository.findById(compId).orElseThrow(() -> new NotFoundException("Подборка с id=" + compId + " не найдена"))
        );
    }

    @Override
    public CompilationDto create(NewCompilationDto compilationDto) {
        if (!compilationRepository.findByTitleIgnoreCase(compilationDto.getTitle()).isEmpty()) {
            throw new ConditionsNotMetException("Подборка с названием " + compilationDto.getTitle() + " уже существует");
        }

        Set<Event> events = new HashSet<>();
        if (!compilationDto.getEvents().isEmpty()) {
            events = new HashSet<>(eventRepository.findAllByIdInAndState(compilationDto.getEvents().stream().toList(), EventState.PUBLISHED));
            List<Long> eventIds = events
                    .stream()
                    .map(Event::getId)
                    .toList();
            if (events.size() != compilationDto.getEvents().size()) {
                List<Long> eventsNotPublished = compilationDto.getEvents().stream().filter(id -> !eventIds.contains(id)).toList();
                throw new ConditionsNotMetException("Не все события в подборке существуют или опубликованы: " + eventsNotPublished);
            }
        }

        return CompilationMapper.toCompilationDto(
                compilationRepository.save(CompilationMapper.newCompilationDtoToCompilation(compilationDto, events))
        );
    }

    @Override
    public CompilationDto update(Long compilationId, UpdateCompilationRequest updateCompilationRequest) {
        Compilation compilation = compilationRepository.findById(compilationId).orElseThrow(() -> new NotFoundException("Подборка с id=" + compilationId + " не найдена"));

        if (updateCompilationRequest.getTitle() != null) {
            if (!compilationRepository.findByTitleIgnoreCase(updateCompilationRequest.getTitle()).isEmpty() &&
                    !compilation.getTitle().equalsIgnoreCase(updateCompilationRequest.getTitle())) {
                throw new ConditionsNotMetException("Подборка с названием " + updateCompilationRequest.getTitle() + " уже существует");
            }
            compilation.setTitle(updateCompilationRequest.getTitle());
        }

        if (updateCompilationRequest.getEvents() != null) {
            Set<Event> events = new HashSet<>();
            if (!updateCompilationRequest.getEvents().isEmpty()) {
                events = new HashSet<>(eventRepository.findAllByIdInAndState(updateCompilationRequest.getEvents().stream().toList(), EventState.PUBLISHED));
                List<Long> eventIds = events
                        .stream()
                        .map(Event::getId)
                        .toList();
                if (events.size() != updateCompilationRequest.getEvents().size()) {
                    List<Long> eventsNotPublished = updateCompilationRequest.getEvents().stream().filter(id -> !eventIds.contains(id)).toList();
                    throw new ConditionsNotMetException("Не все события в подборке существуют или опубликованы: " + eventsNotPublished);
                }
            }
            compilation.setEvents(events);
        }

        if (updateCompilationRequest.getPinned() != null) {
            compilation.setPinned(updateCompilationRequest.getPinned());
        }

        return CompilationMapper.toCompilationDto(compilationRepository.save(compilation));
    }

    @Override
    public void delete(Long compilationId) {
        compilationRepository.findById(compilationId).orElseThrow(() -> new NotFoundException("Подборка c id=" + compilationId + " не найдена"));
        compilationRepository.deleteById(compilationId);
    }
}
