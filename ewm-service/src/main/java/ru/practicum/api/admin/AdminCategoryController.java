package ru.practicum.api.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.core.persistance.model.dto.category.CategoryDto;
import ru.practicum.core.persistance.model.dto.category.NewCategoryDto;
import ru.practicum.core.service.CategoryService;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin/categories")
@Validated
public class AdminCategoryController {

    private final CategoryService categoryService;

    @PostMapping()
    public ResponseEntity<CategoryDto> create(@RequestBody @Valid NewCategoryDto categoryDto) {
        log.info("Получен запрос POST /admin/categories c новой категорией: {}", categoryDto);
        return new ResponseEntity<>(categoryService.create(categoryDto), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CategoryDto> update(@PathVariable Long id, @RequestBody @Valid NewCategoryDto newCategoryDto) {
        log.info("Получен запрос PATCH /admin/categories/{} с обновлённой категорией: {}", id, newCategoryDto);
        return ResponseEntity.ok(categoryService.update(id, newCategoryDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("Получен запрос DELETE /admin/categories/{}", id);
        categoryService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
