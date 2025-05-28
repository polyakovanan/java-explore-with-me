package ru.practicum.core.persistance.model.mapper;

import ru.practicum.core.persistance.model.Compilation;
import ru.practicum.core.persistance.model.Event;
import ru.practicum.core.persistance.model.dto.compilation.CompilationDto;
import ru.practicum.core.persistance.model.dto.compilation.NewCompilationDto;

import java.util.Set;

public class CompilationMapper {
    private CompilationMapper() {

    }

    public static Compilation newCompilationDtoToCompilation(NewCompilationDto dto, Set<Event> events) {
        return Compilation.builder()
                .title(dto.getTitle())
                .pinned(dto.getPinned() != null ? dto.getPinned() : false)
                .events(events)
                .build();
    }

    public static CompilationDto toCompilationDto(Compilation compilation) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .title(compilation.getTitle())
                .pinned(compilation.getPinned())
                .events(compilation.getEvents().stream().map(EventMapper::toEventShortDto).toList())
                .build();
    }
}
