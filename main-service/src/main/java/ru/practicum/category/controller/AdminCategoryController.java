package ru.practicum.category.controller;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.dto.CategoryCreateDto;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.service.CategoryService;

@Slf4j
@Validated
@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto create(@RequestBody @Valid CategoryCreateDto dto) {
        log.info("POST /admin/categories request - create new category: {}", dto);
        return categoryService.create(dto);
    }

    @PatchMapping("/{catId}")
    public CategoryDto update(@PathVariable @Positive Long catId,
                              @RequestBody @Valid CategoryDto dto) {
        log.info("PATCH /admin/categories/{} request - update category : {}", catId, dto);
        return categoryService.update(catId, dto);
    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long catId) {
        log.info("DELETE /admin/categories/{} request - delete category", catId);
        categoryService.delete(catId);
    }


}
