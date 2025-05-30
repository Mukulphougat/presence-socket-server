package org.mukulphougat.presencesocketserver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.mukulphougat.presencesocketserver.entity.OutboxEvent;
import org.mukulphougat.presencesocketserver.repository.OutboxEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@Slf4j
public class OutboxService {

    @Autowired
    private OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public OutboxService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void saveEvent(String eventType, String aggregateId, Object eventPayload) {
        try {
            String payloadJson = objectMapper.writeValueAsString(eventPayload);
            OutboxEvent outboxEvent = new OutboxEvent(eventType, aggregateId, payloadJson);
            outboxEventRepository.save(outboxEvent);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize event payload", e);
        }
    }

    public void saveEvent(OutboxEvent event) {
        outboxEventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public List<OutboxEvent> getUnpublishedEvents(int maxRetries) {
        return outboxEventRepository.findUnpublishedEvents(maxRetries);
    }

    @Transactional(readOnly = true)
    public Page<OutboxEvent> getUnpublishedEventsBatch(int maxRetries, int batchSize) {
        LocalDateTime since = LocalDateTime.now().minusHours(24); // Don't process events older than 24 hours
        Pageable pageable = PageRequest.of(0, batchSize);
        return outboxEventRepository.findUnpublishedEventsSince(maxRetries, since, pageable);
    }

    public void markAsPublished(OutboxEvent event) {
        event.markAsPublished();
        outboxEventRepository.save(event);
    }

    public void markAsFailed(OutboxEvent event, String errorMessage) {
        event.incrementRetryCount();
        event.setError(errorMessage);
        outboxEventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public long getUnpublishedEventCount() {
        return outboxEventRepository.countUnpublishedEvents();
    }

    public List<OutboxEvent> getUnpublishedEventsByAggregateId(String aggregateId) {
        return outboxEventRepository.findByAggregateIdAndPublishedFalse(aggregateId);
    }

    @Scheduled(cron = "0 0 2 * * ?") // Daily at 2 AM
    public void cleanupPublishedEvents() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7); // Keep events for 7 days
        int deletedCount = outboxEventRepository.deletePublishedEventsBefore(cutoffDate);
        log.info("Cleaned up {} published outbox events", deletedCount);
    }
}