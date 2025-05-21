package ru.practicum.api.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.core.persistance.model.dto.compilation.CompilationDto;
import ru.practicum.core.persistance.model.dto.compilation.NewCompilationDto;
import ru.practicum.core.persistance.model.dto.compilation.UpdateCompilationRequest;
import ru.practicum.core.service.CompilationService;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin/compilations")
@Validated
public class AdminCompilationController {
    private final CompilationService compilationService;

    @PostMapping()
    public ResponseEntity<CompilationDto> create(@RequestBody @Valid NewCompilationDto compilationDto) {
        log.info("Получен запрос POST /admin/compilations с новой подборкой: {}", compilationDto);
        return new ResponseEntity<>(compilationService.create(compilationDto), HttpStatus.CREATED);
    }

    @DeleteMapping("/{compId}")
    public ResponseEntity<Void> delete(@PathVariable Long compId) {
        log.info("Получен запрос DELETE /admin/compilations/{}", compId);
        compilationService.delete(compId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PatchMapping("/{compId}")
    public ResponseEntity<CompilationDto> update(@PathVariable Long compId,
                                                 @RequestBody @Valid UpdateCompilationRequest updateCompilationRequest) {
        log.info("Получен запрос PATCH /admin/compilations/{} с обновлённой подборкой: {}", compId, updateCompilationRequest);
        return ResponseEntity.ok(compilationService.update(compId, updateCompilationRequest));
    }
}
