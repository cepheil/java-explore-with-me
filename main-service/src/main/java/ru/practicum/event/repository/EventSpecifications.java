package ru.practicum.event.repository;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.event.enums.EventState;
import ru.practicum.event.model.Event;
import ru.practicum.request.enums.RequestStatus;
import ru.practicum.request.model.ParticipationRequest;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventSpecifications {

    public static Specification<Event> publishedBetween(LocalDateTime start, LocalDateTime end) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.equal(root.get("state"), EventState.PUBLISHED),
                criteriaBuilder.between(root.get("eventDate"), start, end)
        );
    }


    public static Specification<Event> textLike(String text) {
        if (text == null || text.isBlank()) return null;
        return (root, query, criteriaBuilder) -> {
            String p = "%" + text.toLowerCase() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("annotation")), p),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), p)
            );
        };
    }


    public static Specification<Event> categoriesIn(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) return null;
        return (root, query, criteriaBuilder) -> root.get("category").get("id").in(categoryIds);
    }

    public static Specification<Event> paidEq(Boolean paid) {
        if (paid == null) return null;
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("paid"), paid);
    }

    public static Specification<Event> byInitiators(List<Long> users) {
        if (users == null || users.isEmpty()) return null;
        return (root, query, criteriaBuilder) -> root.get("initiator").get("id").in(users);
    }

    public static Specification<Event> byStates(List<EventState> states) {
        if (states == null || states.isEmpty()) return null;
        return (root, query, criteriaBuilder) -> root.get("state").in(states);
    }


    public static Specification<Event> between(LocalDateTime start, LocalDateTime end) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.between(root.get("eventDate"), start, end);
    }

    public static Specification<Event> onlyAvailable(Boolean onlyAvailable) {
        if (!Boolean.TRUE.equals(onlyAvailable)) return null;

        return (root, query, criteriaBuilder) -> {
            var sub = query.subquery(Long.class);
            var r = sub.from(ParticipationRequest.class);
            sub.select(criteriaBuilder.count(r.get("id")))
                    .where(
                            criteriaBuilder.equal(r.get("event").get("id"), root.get("id")),
                            criteriaBuilder.equal(r.get("status"), RequestStatus.CONFIRMED)
                    );
            return criteriaBuilder.or(
                    criteriaBuilder.equal(root.get("participantLimit"), 0),
                    criteriaBuilder.lessThan(sub, criteriaBuilder.toLong(root.get("participantLimit")))
            );
        };
    }

}
