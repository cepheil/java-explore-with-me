package ru.practicum.request.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.request.enums.RequestStatus;
import ru.practicum.request.model.ParticipationRequest;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    boolean existsByEventIdAndRequesterId(Long eventId, Long requesterId);

    @Query("""
            SELECT r.event.id AS eventId, COUNT(r.id) as confirmed
            FROM ParticipationRequest r
            WHERE r.status = :status
              AND r.event.id in :eventIds
            GROUP BY r.event.id
            """)
    List<ConfirmedCount> countConfirmedByEventIds(@Param("eventIds") Collection<Long> eventIds,
                                                  @Param("status") RequestStatus status);


    Page<ParticipationRequest> findAllByRequesterId(Long requesterId, Pageable pageable);

    Optional<ParticipationRequest> findByIdAndRequesterId(Long requestId, Long requesterId);

    long countByEventIdAndStatus(Long eventId, RequestStatus status);

    List<ParticipationRequest> findAllByEventId(Long eventId);

    boolean existsByEventIdAndRequesterIdAndStatus(Long eventId, Long requesterId, RequestStatus status);

    interface ConfirmedCount {
        Long getEventId();

        Long getConfirmed();
    }

}
