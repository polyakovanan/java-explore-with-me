package ru.practicum.api.common;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.core.persistance.model.dto.category.CategoryDto;
import ru.practicum.core.service.CategoryService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/categories")
@Validated
public class CommonCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAll(@RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                                    @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("Получен запрос GET /categories");
        return ResponseEntity.ok(categoryService.getAll(from, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> getById(@PathVariable Long id) {
        log.info("Получен запрос GET /categories/{}", id);
        return ResponseEntity.ok(categoryService.getById(id));
    }
}
