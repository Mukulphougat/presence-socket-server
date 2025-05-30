package org.mukulphougat.presencesocketserver.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.mukulphougat.presencesocketserver.dto.RetryFailedEventsEvent;
import org.mukulphougat.presencesocketserver.entity.OutboxEvent;
import org.mukulphougat.presencesocketserver.dto.UserActivityLog;
import org.mukulphougat.presencesocketserver.service.KafkaEventProducer;
import org.mukulphougat.presencesocketserver.service.OutboxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class OutboxEventPublisher {

    @Autowired
    private OutboxService outboxService;

    @Autowired
    private KafkaEventProducer kafkaEventProducer;
    private final ObjectMapper objectMapper;

    private static final int MAX_RETRIES = 5;
    private static final int BATCH_SIZE = 100;

    public OutboxEventPublisher(
            ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 5000) // Every 5 seconds
    public void publishPendingEvents() {
        try {
            Page<OutboxEvent> eventPage = outboxService.getUnpublishedEventsBatch(MAX_RETRIES, BATCH_SIZE);
            List<OutboxEvent> events = eventPage.getContent();

            if (events.isEmpty()) {
                return;
            }

            log.info("Processing {} unpublished events", events.size());

            for (OutboxEvent event : events) {
                processEvent(event);
            }

        } catch (Exception e) {
            log.error("Error during batch event processing", e);
        }
    }

    private void processEvent(OutboxEvent event) {
        try {
            switch (event.getEventType()) {
                case "USER_ONLINE":
                    publishUserOnlineEvent(event);
                    break;
                case "USER_OFFLINE":
                    publishUserOfflineEvent(event);
                    break;
                default:
                    log.warn("Unknown event type: {}", event.getEventType());
                    outboxService.markAsFailed(event, "Unknown event type");
                    return;
            }

            outboxService.markAsPublished(event);
            log.debug("Successfully published event: {}", event.getId());

        } catch (Exception e) {
            String errorMsg = "Failed to publish event: " + e.getMessage();
            log.error("Failed to publish event {}: {}", event.getId(), errorMsg, e);
            outboxService.markAsFailed(event, errorMsg);
        }
    }

    private void publishUserOnlineEvent(OutboxEvent event) throws Exception {
        UserActivityLog onlineEvent = objectMapper.readValue(event.getPayload(), UserActivityLog.class);
        kafkaEventProducer.sendOnlineEvent(onlineEvent);
    }

    private void publishUserOfflineEvent(OutboxEvent event) throws Exception {
        UserActivityLog offlineEvent = objectMapper.readValue(event.getPayload(), UserActivityLog.class);
        kafkaEventProducer.sendOfflineEvent(offlineEvent);
    }


    // Manual retry for failed events
    // Manual retry for failed events
    @EventListener
    @Async
    public void handleRetryFailedEvents(RetryFailedEventsEvent retryEvent) {
        log.info("Manually retrying failed events triggered at: {}", retryEvent.getTriggeredAt());

        try {
            List<OutboxEvent> failedEvents = outboxService.getUnpublishedEvents(MAX_RETRIES + 10); // Get even highly failed events

            if (failedEvents.isEmpty()) {
                log.info("No failed events found for manual retry");
                return;
            }

            log.info("Found {} failed events for manual retry", failedEvents.size());
            int successCount = 0;
            int failureCount = 0;

            for (OutboxEvent event : failedEvents) {
                if (event.getRetryCount() >= MAX_RETRIES) {
                    try {
                        log.debug("Manually retrying event: {} (previous attempts: {})",
                                event.getId(), event.getRetryCount());

                        // Reset retry count for manual retry
                        event.setRetryCount(0);
                        event.setErrorMessage(null);
                        outboxService.saveEvent(event);

                        // Process the event
                        processEvent(event);
                        successCount++;

                    } catch (Exception e) {
                        log.error("Manual retry failed for event: {}", event.getId(), e);
                        failureCount++;
                        // Don't reset retry count if manual retry also fails
                        event.setRetryCount(MAX_RETRIES);
                        event.setError("Manual retry failed: " + e.getMessage());
                        outboxService.saveEvent(event);
                    }
                }
            }

            log.info("Manual retry completed. Success: {}, Failures: {}", successCount, failureCount);

        } catch (Exception e) {
            log.error("Error during manual retry process", e);
        }
    }
}