package org.mukulphougat.presencesocketserver.repository;

import org.mukulphougat.presencesocketserver.entity.OutboxEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    @Query("SELECT o FROM OutboxEvent o WHERE o.published = false AND o.retryCount < :maxRetries ORDER BY o.createdAt ASC")
    List<OutboxEvent> findUnpublishedEvents(@Param("maxRetries") int maxRetries);

    @Query("SELECT o FROM OutboxEvent o WHERE o.published = false AND o.retryCount < :maxRetries AND o.createdAt >= :since ORDER BY o.createdAt ASC")
    Page<OutboxEvent> findUnpublishedEventsSince(@Param("maxRetries") int maxRetries,
                                                 @Param("since") LocalDateTime since,
                                                 Pageable pageable);

    @Query("SELECT COUNT(o) FROM OutboxEvent o WHERE o.published = false")
    long countUnpublishedEvents();

    @Modifying
    @Query("DELETE FROM OutboxEvent o WHERE o.published = true AND o.publishedAt < :cutoffDate")
    int deletePublishedEventsBefore(@Param("cutoffDate") LocalDateTime cutoffDate);

    List<OutboxEvent> findByAggregateIdAndPublishedFalse(String aggregateId);
}