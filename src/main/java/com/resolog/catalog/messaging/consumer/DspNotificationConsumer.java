package com.resolog.catalog.messaging.consumer;

import com.resolog.catalog.messaging.KafkaConstants;
import com.resolog.catalog.service.ProcessedEventService;
import com.resolog.catalog.messaging.event.ProductPublished;
import com.resolog.catalog.messaging.event.ProductUnpublished;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
@Slf4j
public class DspNotificationConsumer {

    private final ProcessedEventService processedEventService;
    private final ObjectMapper objectMapper;

    public DspNotificationConsumer(
            ProcessedEventService processedEventService,
            ObjectMapper objectMapper) {
        this.processedEventService = processedEventService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = KafkaConstants.PRODUCT_PUBLISHED,
            groupId = KafkaConstants.GROUP_DSP_SERVICE)
    @Transactional
    public void handlePublished(
            @Payload String payload,
            @Header(KafkaConstants.HEADER_MESSAGE_ID) byte[] messageIdBytes,
            Acknowledgment ack) {
        UUID messageId = UUID.fromString(new String(messageIdBytes, StandardCharsets.UTF_8));
        if (processedEventService.hasBeenProcessed(messageId, ack)) {
            return;
        }

        try {
            ProductPublished event = objectMapper.readValue(payload, ProductPublished.class);
            log.info("Notifying DSPs of published product {}", event.aggregateId());

            processedEventService.markAsProcessed(messageId);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Failed to notify DSPs for messageId {}", messageId, e);
            throw e;
        }
    }

    @KafkaListener(
            topics = KafkaConstants.PRODUCT_UNPUBLISHED,
            groupId = KafkaConstants.GROUP_DSP_SERVICE)
    @Transactional
    public void handleUnpublished(
            @Payload String payload,
            @Header(KafkaConstants.HEADER_MESSAGE_ID) byte[] messageIdBytes,
            Acknowledgment ack) {
        UUID messageId = UUID.fromString(new String(messageIdBytes, StandardCharsets.UTF_8));
        if (processedEventService.hasBeenProcessed(messageId, ack)) {
            return;
        }

        try {
            ProductUnpublished event = objectMapper.readValue(payload, ProductUnpublished.class);
            log.info("Notifying DSPs to remove unpublished product {}", event.aggregateId());

            processedEventService.markAsProcessed(messageId);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Failed to notify DSPs for messageId {}", messageId, e);
            throw e;
        }
    }
}
