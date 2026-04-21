package com.resolog.catalog.messaging.topic;

import com.resolog.catalog.messaging.event.Event;
import com.resolog.catalog.messaging.event.ProductCreated;
import com.resolog.catalog.messaging.event.ProductDeleted;
import com.resolog.catalog.messaging.event.ProductPublished;
import com.resolog.catalog.messaging.event.ProductRejected;
import com.resolog.catalog.messaging.event.ProductSubmissionApproved;
import com.resolog.catalog.messaging.event.ProductSubmissionDeclined;
import com.resolog.catalog.messaging.event.ProductSubmittedForPublishing;
import com.resolog.catalog.messaging.event.ProductUnpublished;

import java.util.Map;
import java.util.Optional;

import static com.resolog.catalog.messaging.KafkaConstants.PRODUCT_CREATED;
import static com.resolog.catalog.messaging.KafkaConstants.PRODUCT_DELETED;
import static com.resolog.catalog.messaging.KafkaConstants.PRODUCT_PUBLISHED;
import static com.resolog.catalog.messaging.KafkaConstants.PRODUCT_REJECTED;
import static com.resolog.catalog.messaging.KafkaConstants.PRODUCT_SUBMISSION_APPROVED;
import static com.resolog.catalog.messaging.KafkaConstants.PRODUCT_SUBMISSION_DECLINED;
import static com.resolog.catalog.messaging.KafkaConstants.PRODUCT_SUBMITTED_FOR_PUBLISHING;
import static com.resolog.catalog.messaging.KafkaConstants.PRODUCT_UNPUBLISHED;

public final class KafkaTopics {

    private static final Map<String, String> TOPIC_MAP = Map.of(
            ProductCreated.class.getSimpleName(),                   PRODUCT_CREATED,
            ProductSubmittedForPublishing.class.getSimpleName(),    PRODUCT_SUBMITTED_FOR_PUBLISHING,
            ProductPublished.class.getSimpleName(),                 PRODUCT_PUBLISHED,
            ProductRejected.class.getSimpleName(),                  PRODUCT_REJECTED,
            ProductUnpublished.class.getSimpleName(),               PRODUCT_UNPUBLISHED,
            ProductDeleted.class.getSimpleName(),                   PRODUCT_DELETED,
            ProductSubmissionApproved.class.getSimpleName(),        PRODUCT_SUBMISSION_APPROVED,
            ProductSubmissionDeclined.class.getSimpleName(),        PRODUCT_SUBMISSION_DECLINED
    );

    public static String topicFor(Class<? extends Event> clazz) {
        return topicFor(clazz.getSimpleName());
    }

    public static String topicFor(String eventType) {
        return Optional.ofNullable(TOPIC_MAP.get(eventType))
                .orElseThrow(() -> new IllegalArgumentException("Unknown event type: " + eventType));
    }

    private KafkaTopics() {}

}
