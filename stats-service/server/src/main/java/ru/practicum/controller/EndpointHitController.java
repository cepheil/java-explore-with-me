package ru.practicum.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.DateTimeConstants;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.service.EndpointHitService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping
public class EndpointHitController {

    private final EndpointHitService service;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void createHit(@Valid @RequestBody EndpointHitDto dto) {
        log.info("POST /hit -> app={}, uri={}, ip={}, ts={}", dto.getApp(), dto.getUri(), dto.getIp(), dto.getTimestamp());
        service.saveHit(dto);
    }

    @GetMapping("/stats")
    @ResponseStatus(HttpStatus.OK)
    public List<ViewStatsDto> getStats(
            @RequestParam @DateTimeFormat(pattern = DateTimeConstants.PATTERN) LocalDateTime start,
            @RequestParam @DateTimeFormat(pattern = DateTimeConstants.PATTERN) LocalDateTime end,
            @RequestParam(required = false) List<String> uris,
            @RequestParam(required = false) Boolean unique
    ) {
        log.info("GET /stats -> start={}, end={}, uris={}, unique={}", start, end, uris, unique);
        return service.getStats(start, end, uris, unique);
    }

}
