package ru.practicum.comment.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.comment.model.Comment;

import java.time.LocalDateTime;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {


    @Query("""
                SELECT c
                  FROM Comment c
                 WHERE c.event.id = :eventId
                   AND (:authorId IS NULL OR c.author.id = :authorId)
                   AND (:start    IS NULL OR c.created >= :start)
                   AND (:end      IS NULL OR c.created <= :end)
                 ORDER BY c.created DESC
            """)
    Page<Comment> searchEventComments(@Param("eventId") Long eventId,
                                      @Param("authorId") Long authorId,
                                      @Param("start") LocalDateTime start,
                                      @Param("end") LocalDateTime end,
                                      Pageable pageable);


    @Query("""
                SELECT c
                  FROM Comment c
                 WHERE c.author.id = :authorId
                   AND (:start IS NULL OR c.created >= :start)
                   AND (:end   IS NULL OR c.created <= :end)
                 ORDER BY c.created DESC
            """)
    Page<Comment> searchAuthorComments(@Param("authorId") Long authorId,
                                       @Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end,
                                       Pageable pageable);


    Optional<Comment> findByIdAndAuthorId(Long id, Long authorId);


    long deleteByIdAndAuthorId(Long id, Long authorId);


}
