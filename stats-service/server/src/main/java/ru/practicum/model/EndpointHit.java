package ru.practicum.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "endpoint_hit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EndpointHit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "app", nullable = false, length = 100)
    private String app;

    @Column(name = "uri", nullable = false, length = 255, columnDefinition = "text")
    private String uri;

    @Column(name = "ip", nullable = false, length = 64)
    private String ip;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;


}
