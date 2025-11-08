package ru.practicum.compilation.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationDto;
import ru.practicum.compilation.service.CompilationService;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/compilations")
public class CompilationAdminController {

    private final CompilationService service;

    // POST /admin/compilations
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto create(@Valid @RequestBody NewCompilationDto dto) {
        log.debug("Processing request to create compilation with DTO: {}", dto);
        return service.create(dto);
    }

    // DELETE /admin/compilations/{compId}
    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long compId) {
        log.debug("Processing request to delete compilation with ID: {}", compId);
        service.delete(compId);
    }

    // PATCH /admin/compilations/{compId}
    @PatchMapping("/{compId}")
    public CompilationDto update(@PathVariable Long compId,
                                 @Valid @RequestBody UpdateCompilationDto dto) {
        log.debug("Processing request to update compilation ID: {} with DTO: {}", compId, dto);
        return service.update(compId, dto);
    }

}
