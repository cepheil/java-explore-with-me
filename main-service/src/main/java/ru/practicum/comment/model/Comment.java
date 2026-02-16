package ru.practicum.comment.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.practicum.event.model.Event;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;


    @Column(name = "text", nullable = false, length = 2000)
    @Size(max = 2000)
    private String text;


    @Column(name = "created", nullable = false)
    private LocalDateTime created;


    @Column(name = "updated")
    private LocalDateTime updated;

}
