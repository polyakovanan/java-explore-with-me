package ru.practicum.core.service;

import ru.practicum.core.persistance.model.dto.compilation.CompilationDto;
import ru.practicum.core.persistance.model.dto.compilation.NewCompilationDto;
import ru.practicum.core.persistance.model.dto.compilation.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {

    List<CompilationDto> findAll(Boolean pinned, Integer from, Integer size);

    CompilationDto findById(Long compId);

    CompilationDto create(NewCompilationDto compilationDto);

    CompilationDto update(Long compilationId, UpdateCompilationRequest updateCompilationRequest);

    void delete(Long compilationId);
}
