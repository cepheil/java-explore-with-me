package ru.practicum.client;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.practicum.dto.DateTimeConstants;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import org.springframework.http.HttpStatusCode;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class StatClient {

    private final RestClient restClient;

    public StatClient(@Value("${stats-server.url}") String baseUrl) {
        this.restClient = RestClient.create(baseUrl);
        log.info("Stats client initialized. Base URL: {}", baseUrl);
    }

    public void saveHit(EndpointHitDto hit) {
        try {
            restClient.post()
                    .uri("/hit")
                    .body(hit)
                    .retrieve()
                    .toBodilessEntity();

            log.debug("Hit sent to stats-service: app={}, uri={}, ip={}", hit.getApp(), hit.getUri(), hit.getIp());
        } catch (Exception e) {
            log.warn("Hit skipped: {}", e.toString());
        }
    }

    public List<ViewStatsDto> getStats(LocalDateTime start,
                                       LocalDateTime end,
                                       List<String> uris,
                                       Boolean unique) {
        try {
            if (start == null || end == null) {
                log.debug("Stats skipped: start/end is null");
                return Collections.emptyList();
            }

            List<ViewStatsDto> result = restClient.get()
                    .uri(b -> {
                        var ub = b.path("/stats")
                                .queryParam("start", start.format(DateTimeConstants.FORMATTER))
                                .queryParam("end", end.format(DateTimeConstants.FORMATTER));
                        if (uris != null && !uris.isEmpty()) {
                            uris.forEach(u -> ub.queryParam("uris", u));
                        }
                        if (unique != null) {
                            ub.queryParam("unique", unique);
                        }
                        return ub.build();
                    })
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        log.error("Stats service error: {} {}", response.getStatusCode(), response.getStatusText());
                    })
                    .body(new ParameterizedTypeReference<List<ViewStatsDto>>() {
                    });

            if (result != null && !result.isEmpty()) {
                log.debug("Stats received successfully: {} records", result.size());
            } else {
                log.debug("Stats response is empty");
            }

            return result != null ? result : Collections.emptyList();

        } catch (Exception e) {
            log.error("Failed to get stats from stats-service: {}", e.toString());
            return Collections.emptyList();
        }

    }

}
