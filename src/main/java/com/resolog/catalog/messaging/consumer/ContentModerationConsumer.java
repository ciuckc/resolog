package com.resolog.catalog.messaging.consumer;

import com.resolog.catalog.messaging.KafkaConstants;
import com.resolog.catalog.service.ProcessedEventService;
import com.resolog.catalog.messaging.event.ProductSubmissionApproved;
import com.resolog.catalog.messaging.event.ProductSubmissionDeclined;
import com.resolog.catalog.messaging.event.ProductSubmittedForPublishing;
import com.resolog.catalog.messaging.event.TrackPayload;
import com.resolog.catalog.service.OutboxEventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class ContentModerationConsumer {

    private final OutboxEventService outboxEventService;
    private final ProcessedEventService processedEventService;
    private final ObjectMapper objectMapper;

    public ContentModerationConsumer(
            OutboxEventService outboxEventService,
            ProcessedEventService processedEventService,
            ObjectMapper objectMapper) {
        this.outboxEventService = outboxEventService;
        this.processedEventService = processedEventService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = KafkaConstants.PRODUCT_SUBMITTED_FOR_PUBLISHING,
            groupId = KafkaConstants.GROUP_MODERATION_SERVICE)
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
            ProductSubmittedForPublishing event = objectMapper.readValue(payload, ProductSubmittedForPublishing.class);
            log.info("Moderating product {} - title: {}", event.aggregateId(), event.title());

            String rejectionReason = validate(event);

            if (rejectionReason != null) {
                outboxEventService.publish(event.aggregateId(), new ProductSubmissionDeclined(event.aggregateId(), rejectionReason));
                log.info("Product {} declined - {}", event.aggregateId(), rejectionReason);
            } else {
                outboxEventService.publish(event.aggregateId(), new ProductSubmissionApproved(event.aggregateId()));
                log.info("Product {} approved for publishing", event.aggregateId());
            }

            processedEventService.markAsProcessed(messageId);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Failed to process moderation event for messageId {}", messageId, e);
            throw e;
        }
    }

    private String validate(ProductSubmittedForPublishing event) {
        if (event.price() == null) {
            return "Product must have a price";
        }
        if (event.tracks().isEmpty()) {
            return "Product must have at least one track";
        }
        if (!isValidUrl(event.artworkUrl())) {
            return "Invalid artwork URL";
        }
        for (TrackPayload track : event.tracks()) {
            if (!isValidUrl(track.audioUrl())) {
                return "Invalid audio URL for track " + track.trackNumber();
            }
        }
        if (!areTrackNumbersSequential(event.tracks())) {
            return "Track numbers are not sequential starting from 1";
        }
        return null;
    }

    private boolean isValidUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        try {
            new URI(url).toURL();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean areTrackNumbersSequential(List<TrackPayload> tracks) {
        List<Integer> numbers = tracks.stream()
                .map(TrackPayload::trackNumber)
                .sorted()
                .toList();
        for (int i = 0; i < numbers.size(); i++) {
            if (numbers.get(i) != i + 1) {
                return false;
            }
        }
        return true;
    }
}
