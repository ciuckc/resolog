package com.resolog.catalog.messaging.consumer;

import com.resolog.catalog.TestFixtures;
import com.resolog.catalog.domain.model.ProductStatus;
import com.resolog.catalog.messaging.event.ProductSubmissionApproved;
import com.resolog.catalog.messaging.event.ProductSubmissionDeclined;
import com.resolog.catalog.messaging.event.ProductSubmittedForPublishing;
import com.resolog.catalog.messaging.event.TrackPayload;
import com.resolog.catalog.service.OutboxEventService;
import com.resolog.catalog.service.ProcessedEventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ContentModerationConsumerTest {

    @Mock
    private OutboxEventService outboxEventService;

    @Mock
    private ProcessedEventService processedEventService;

    @Mock
    private Acknowledgment ack;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private ContentModerationConsumer consumer;

    private final UUID productId = UUID.randomUUID();

    private final byte[] messageIdBytes = UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8);

    @BeforeEach
    void setUp() {
        consumer = new ContentModerationConsumer(outboxEventService, processedEventService, objectMapper);
    }

    @Test
    void handle_approvesValidProduct() throws Exception {
        String payload = objectMapper.writeValueAsString(
                TestFixtures.aProductSubmittedForPublishing(productId, List.of(TestFixtures.aTrackPayload(1))));

        consumer.handle(payload, messageIdBytes, ack);

        verify(outboxEventService).publish(eq(productId), isA(ProductSubmissionApproved.class));
    }

    @Test
    void handle_approvesValidMultiTrackProduct() throws Exception {
        String payload = objectMapper.writeValueAsString(
                TestFixtures.aProductSubmittedForPublishing(productId, List.of(
                        TestFixtures.aTrackPayload(1),
                        TestFixtures.aTrackPayload(2))));

        consumer.handle(payload, messageIdBytes, ack);

        verify(outboxEventService).publish(eq(productId), isA(ProductSubmissionApproved.class));
    }

    @Test
    void handle_declinesWhenNoTracks() throws Exception {
        String payload = objectMapper.writeValueAsString(
                TestFixtures.aProductSubmittedForPublishing(productId, List.of()));

        consumer.handle(payload, messageIdBytes, ack);

        ArgumentCaptor<ProductSubmissionDeclined> captor = ArgumentCaptor.forClass(ProductSubmissionDeclined.class);
        verify(outboxEventService).publish(eq(productId), captor.capture());
        assertEquals("Product must have at least one track", captor.getValue().reason());
    }

    @Test
    void handle_declinesWhenArtworkUrlIsInvalid() throws Exception {
        var event = new ProductSubmittedForPublishing(
                productId,
                TestFixtures.PRODUCT_TYPE,
                ProductStatus.DRAFT,
                TestFixtures.PRODUCT_TITLE,
                TestFixtures.PRODUCT_GENRE,
                TestFixtures.PRODUCT_RELEASE_DATE,
                "not-a-url",
                TestFixtures.PRODUCT_PRICE,
                Set.of(),
                List.of(TestFixtures.aTrackPayload(1)));

        consumer.handle(objectMapper.writeValueAsString(event), messageIdBytes, ack);

        ArgumentCaptor<ProductSubmissionDeclined> captor = ArgumentCaptor.forClass(ProductSubmissionDeclined.class);
        verify(outboxEventService).publish(eq(productId), captor.capture());
        assertEquals("Invalid artwork URL", captor.getValue().reason());
    }

    @Test
    void handle_declinesWhenTrackAudioUrlIsInvalid() throws Exception {
        String payload = objectMapper.writeValueAsString(
                TestFixtures.aProductSubmittedForPublishing(productId, List.of(new TrackPayload(1, "not-a-url"))));

        consumer.handle(payload, messageIdBytes, ack);

        verify(outboxEventService).publish(eq(productId), isA(ProductSubmissionDeclined.class));
    }

    @Test
    void handle_declinesWhenTrackNumbersNotSequential() throws Exception {
        String payload = objectMapper.writeValueAsString(
                TestFixtures.aProductSubmittedForPublishing(productId, List.of(
                        TestFixtures.aTrackPayload(1),
                        TestFixtures.aTrackPayload(3))));

        consumer.handle(payload, messageIdBytes, ack);

        verify(outboxEventService).publish(eq(productId), isA(ProductSubmissionDeclined.class));
    }

    @Test
    void handle_declinesWhenTrackNumbersDoNotStartAtOne() throws Exception {
        String payload = objectMapper.writeValueAsString(
                TestFixtures.aProductSubmittedForPublishing(productId, List.of(TestFixtures.aTrackPayload(2))));

        consumer.handle(payload, messageIdBytes, ack);

        verify(outboxEventService).publish(eq(productId), isA(ProductSubmissionDeclined.class));
    }
}
