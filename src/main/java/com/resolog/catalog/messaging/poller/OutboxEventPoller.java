package com.resolog.catalog.messaging.poller;

import com.resolog.catalog.domain.model.OutboxEvent;
import com.resolog.catalog.domain.model.OutboxEventStatus;
import com.resolog.catalog.domain.repository.OutboxEventRepository;
import com.resolog.catalog.messaging.KafkaConstants;
import com.resolog.catalog.messaging.topic.KafkaTopics;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@Slf4j
public class OutboxEventPoller {

    private static final long POOL_INTERVAL_MS = 1_000L;

    private final OutboxEventRepository outboxEventRepository;

    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxEventPoller(
            OutboxEventRepository outboxEventRepository,
            KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = POOL_INTERVAL_MS)
    @Transactional
    public void poll() {
        List<OutboxEvent> pending = outboxEventRepository.findByStatus(OutboxEventStatus.PENDING);

        for (OutboxEvent event : pending) {
            try {
                ProducerRecord<String, String> record = new ProducerRecord<>(
                        KafkaTopics.topicFor(event.getEventType()),
                        event.getAggregateId().toString(),
                        event.getPayload()
                );
                record.headers()
                        .add(
                                KafkaConstants.HEADER_MESSAGE_ID,
                                event.getId().toString().getBytes(StandardCharsets.UTF_8));

                kafkaTemplate.send(record).get();

                event.markSent();
                outboxEventRepository.save(event);
            } catch (Exception e) {
                log.error("Failed to publish outbox event {}", event.getId(), e);
            }
        }
    }
}
