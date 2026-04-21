package com.resolog.catalog.messaging.consumer;

import com.resolog.catalog.messaging.KafkaConstants;
import com.resolog.catalog.service.ProcessedEventService;
import com.resolog.catalog.messaging.event.ProductSubmissionApproved;
import com.resolog.catalog.messaging.event.ProductSubmissionDeclined;
import com.resolog.catalog.service.ProductService;
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
public class ModerationResultConsumer {

    private final ProductService productService;
    private final ProcessedEventService processedEventService;
    private final ObjectMapper objectMapper;

    public ModerationResultConsumer(
            ProductService productService,
            ProcessedEventService processedEventService,
            ObjectMapper objectMapper) {
        this.productService = productService;
        this.processedEventService = processedEventService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = KafkaConstants.PRODUCT_SUBMISSION_APPROVED,
            groupId = KafkaConstants.GROUP_CATALOG_SERVICE)
    @Transactional
    public void handleApproved(
            @Payload String payload,
            @Header(KafkaConstants.HEADER_MESSAGE_ID) byte[] messageIdBytes,
            Acknowledgment ack) {
        UUID messageId = UUID.fromString(new String(messageIdBytes, StandardCharsets.UTF_8));
        if (processedEventService.hasBeenProcessed(messageId, ack)) {
            return;
        }

        try {
            ProductSubmissionApproved event = objectMapper.readValue(payload, ProductSubmissionApproved.class);
            productService.confirmPublished(event.aggregateId());
            log.info("Product {} confirmed as published", event.aggregateId());

            processedEventService.markAsProcessed(messageId);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Failed to handle approval for messageId {}", messageId, e);
            throw e;
        }
    }

    @KafkaListener(
            topics = KafkaConstants.PRODUCT_SUBMISSION_DECLINED,
            groupId = KafkaConstants.GROUP_CATALOG_SERVICE)
    @Transactional
    public void handleDeclined(
            @Payload String payload,
            @Header(KafkaConstants.HEADER_MESSAGE_ID) byte[] messageIdBytes,
            Acknowledgment ack) {
        UUID messageId = UUID.fromString(new String(messageIdBytes, StandardCharsets.UTF_8));
        if (processedEventService.hasBeenProcessed(messageId, ack)) {
            return;
        }

        try {
            ProductSubmissionDeclined event = objectMapper.readValue(payload, ProductSubmissionDeclined.class);
            productService.rejectProduct(event.aggregateId(), event.reason());
            log.info("Product {} declined with reason: {}", event.aggregateId(), event.reason());

            processedEventService.markAsProcessed(messageId);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Failed to handle decline for messageId {}", messageId, e);
            throw e;
        }
    }


}
