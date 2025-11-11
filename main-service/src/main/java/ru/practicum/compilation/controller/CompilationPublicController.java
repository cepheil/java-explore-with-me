package ru.practicum.compilation.controller;


import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.service.CompilationService;

import java.util.List;


@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/compilations")
public class CompilationPublicController {

    private final CompilationService service;


    // GET /compilations?pinned=&from=&size=
    @GetMapping
    public List<CompilationDto> findAll(@RequestParam(required = false) Boolean pinned,
                                        @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                        @RequestParam(defaultValue = "10") @Positive int size) {
        log.debug("Processing request to find compilations with params: pinned={}, from={}, size={}", pinned, from, size);
        return service.findAll(pinned, from, size);
    }

    // GET /compilations/{compId}
    @GetMapping("/{compId}")
    public CompilationDto getById(@PathVariable Long compId) {
        log.debug("Processing request to get compilation by ID: {}", compId);
        return service.getById(compId);
    }

}
