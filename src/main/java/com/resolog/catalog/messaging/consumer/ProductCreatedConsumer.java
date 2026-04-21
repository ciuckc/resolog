package com.resolog.catalog.messaging.consumer;

import com.resolog.catalog.messaging.KafkaConstants;
import com.resolog.catalog.messaging.event.ProductCreated;
import com.resolog.catalog.service.ProcessedEventService;
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
public class ProductCreatedConsumer {

    private final ProcessedEventService processedEventService;
    private final ObjectMapper objectMapper;

    public ProductCreatedConsumer(ProcessedEventService processedEventService,
                                  ObjectMapper objectMapper) {
        this.processedEventService = processedEventService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = KafkaConstants.PRODUCT_CREATED,
            groupId = KafkaConstants.GROUP_DASHBOARD_SERVICE)
    @Transactional
    public void handle(
            @Payload String payload,
            @Header(KafkaConstants.HEADER_MESSAGE_ID) byte[] messageIdBytes,
            Acknowledgment ack) {
        UUID messageId = UUID.fromString(new String(messageIdBytes, StandardCharsets.UTF_8));
        if (processedEventService.hasBeenProcessed(messageId, ack)) {
            return;
        }

        try {
            ProductCreated event = objectMapper.readValue(payload, ProductCreated.class);
            log.info("Updating dashboard for created product {}", event.aggregateId());

            processedEventService.markAsProcessed(messageId);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Failed to update dashboard for messageId {}", messageId, e);
            throw e;
        }
    }
}
