package ru.practicum.request.model;


import jakarta.persistence.*;
import lombok.*;
import ru.practicum.event.model.Event;
import ru.practicum.request.enums.RequestStatus;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "participation_requests",
        uniqueConstraints = {@UniqueConstraint(name = "uq_requester_event", columnNames = {"requester_id", "event_id"})}
)
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private RequestStatus status;

    @Column(name = "created", nullable = false)
    private LocalDateTime created;

}
