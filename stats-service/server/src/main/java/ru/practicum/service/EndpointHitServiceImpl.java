package ru.practicum.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.EndpointHitMapper;
import ru.practicum.model.EndpointHit;
import ru.practicum.repository.EndpointHitRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EndpointHitServiceImpl implements EndpointHitService {

    private final EndpointHitRepository repository;


    @Override
    @Transactional
    public EndpointHit saveHit(EndpointHitDto dto) {
        if (dto == null) {
            log.warn("Body must not be null");
            throw new ValidationException("Body must not be null");
        }

        if (dto.getApp() == null || dto.getApp().trim().isBlank()) {
            log.warn("app must not be blank");
            throw new ValidationException("app must not be blank");
        }

        if (dto.getUri() == null || dto.getUri().trim().isBlank()) {
            log.warn("uri must not be blank");
            throw new ValidationException("uri must not be blank");
        }
        if (dto.getIp() == null || dto.getIp().trim().isBlank()) {
            log.warn("ip must not be blank");
            throw new ValidationException("ip must not be blank");
        }

        if (dto.getTimestamp() == null) {
            log.warn("timestamp must not be null");
            throw new ValidationException("timestamp must not be null");
        }

        EndpointHit hit = EndpointHitMapper.toEntity(dto);
        EndpointHit saved = repository.save(hit);
        log.debug("Saved hit: id={}, app={}, uri={}, ip={}, ts={}",
                saved.getId(), saved.getApp(), saved.getUri(), saved.getIp(), saved.getTimestamp());
        return saved;
    }


    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start,
                                       LocalDateTime end,
                                       List<String> uris,
                                       Boolean unique) {
        validateRange(start, end);

        List<String> filterUris = (CollectionUtils.isEmpty(uris) ? null : uris);

        List<ViewStatsDto> result = Boolean.TRUE.equals(unique)
                ? repository.getUniqueStats(start, end, filterUris)
                : repository.getStats(start, end, filterUris);

        log.debug("Stats queried: start={}, end={}, urisCount={}, unique={}, size={}",
                start, end, filterUris == null ? 0 : filterUris.size(), unique, result.size());

        return result;
    }

    private void validateRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            log.warn("start and end must be provided");
            throw new ValidationException("start and end must be provided");
        }

        if (start.isAfter(end)) {
            log.warn("start must be before or equal to end");
            throw new ValidationException("start must be before or equal to end");
        }

    }

}
