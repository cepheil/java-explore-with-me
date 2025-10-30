package ru.practicum.client;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.dto.DateTimeConstants;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import org.springframework.http.HttpStatusCode;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class StatClient {

    private final RestClient restClient;
    private final String baseUrl;

    public StatClient(@Value("${stats-server.url}") String baseUrl) {
        this.baseUrl = baseUrl;
        this.restClient = RestClient.create(baseUrl);
    }

    public void saveHit(EndpointHitDto hit) {
        try {
            restClient.post()
                    .uri("/hit")
                    .body(hit).retrieve()
                    .toBodilessEntity();

            log.debug("Hit sent to stats-service: app={}, uri={}, ip={}", hit.getApp(), hit.getUri(), hit.getIp());

        } catch (RestClientException e) {
            log.error("Failed to send hit to stats-service: {}", e.getMessage());
        }
    }

    public List<ViewStatsDto> getStats(LocalDateTime start,
                                       LocalDateTime end,
                                       List<String> uris,
                                       Boolean unique) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/stats")
                    .queryParam("start", start.format(DateTimeConstants.FORMATTER))
                    .queryParam("end", end.format(DateTimeConstants.FORMATTER));

            if (uris != null && !uris.isEmpty()) {
                uris.forEach(uri -> builder.queryParam("uris", uri));
            }

            if (unique != null) {
                builder.queryParam("unique", unique);
            }

            URI uri = builder.build(true).toUri();

            List<ViewStatsDto> result = restClient.get()
                    .uri(uri)
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

        } catch (RestClientException e) {
            log.error("Failed to get stats from stats-service: {}", e.getMessage());
            return List.of();
        }

    }


}
