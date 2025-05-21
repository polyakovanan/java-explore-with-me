package ru.practicum.api.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.core.persistance.model.dto.compilation.CompilationDto;
import ru.practicum.core.service.CompilationService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/compilations")
@Validated
public class CommonCompilationController {
    private final CompilationService compilationService;
    @GetMapping
    public ResponseEntity<List<CompilationDto>> findAll(@RequestParam(required = false) Boolean pinned,
                                                        @RequestParam(defaultValue = "0") Integer from,
                                                        @RequestParam(defaultValue = "10") Integer size) {
        log.info("Получен запрос GET /compilations");
        return ResponseEntity.ok(compilationService.findAll(pinned, from, size));
    }

    @GetMapping("/{compId}")
    public ResponseEntity<CompilationDto> findById(@PathVariable Long compId) {
        log.info("Получен запрос GET /compilations/{}", compId);
        return ResponseEntity.ok(compilationService.findById(compId));
    }
}
