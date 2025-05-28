src/
├── config/
│   └── WebSocketConfig.java
├── controller/
│   └── PresenceSocketHandler.java
├── service/
│   ├── RedisPresenceService.java
│   ├── OutboxService.java
│   └── KafkaProducerService.java
├── entity/
│   └── OutboxEvent.java
├── repository/
│   └── OutboxEventRepository.java
├── scheduler/
│   └── OutboxEventPublisher.java
└── util/
└── JwtUtil.java