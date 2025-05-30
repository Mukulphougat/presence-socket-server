package org.mukulphougat.presencesocketserver.dto;

import java.time.LocalDateTime;

public class RetryFailedEventsEvent {
    private final LocalDateTime triggeredAt;
    private final String triggeredBy;
    private final String reason;

    public RetryFailedEventsEvent() {
        this.triggeredAt = LocalDateTime.now();
        this.triggeredBy = "SYSTEM";
        this.reason = "Manual retry requested";
    }

    public RetryFailedEventsEvent(String triggeredBy, String reason) {
        this.triggeredAt = LocalDateTime.now();
        this.triggeredBy = triggeredBy;
        this.reason = reason;
    }

    public LocalDateTime getTriggeredAt() { return triggeredAt; }
    public String getTriggeredBy() { return triggeredBy; }
    public String getReason() { return reason; }
}