package org.mukulphougat.presencesocketserver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mukulphougat.presencesocketserver.dto.UserActivityLog;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaEventProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String USER_ONLINE_TOPIC = "user-online-events";
    private static final String USER_OFFLINE_TOPIC = "user-offline-events";
    public void sendOnlineEvent(UserActivityLog event) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(event);  // Convert object to JSON

            kafkaTemplate.send(USER_ONLINE_TOPIC, json);
            log.info("🔹 Sent User Online Kafka Event: {}", json);
        } catch (Exception e) {
            log.error("❌ Failed to serialize and send Kafka event", e);
        }
    }
    public void sendOfflineEvent(UserActivityLog event) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(event);  // Convert object to JSON

            kafkaTemplate.send(USER_OFFLINE_TOPIC, json);
            log.info("🔹 Sent User Offline Kafka Event: {}", json);
        } catch (Exception e) {
            log.error("❌ Failed to serialize and send Kafka event", e);
        }
    }
}
