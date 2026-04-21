package com.resolog.catalog.config;

import com.resolog.catalog.messaging.topic.KafkaTopics;
import com.resolog.catalog.messaging.event.ProductCreated;
import com.resolog.catalog.messaging.event.ProductDeleted;
import com.resolog.catalog.messaging.event.ProductPublished;
import com.resolog.catalog.messaging.event.ProductRejected;
import com.resolog.catalog.messaging.event.ProductSubmissionApproved;
import com.resolog.catalog.messaging.event.ProductSubmissionDeclined;
import com.resolog.catalog.messaging.event.ProductSubmittedForPublishing;
import com.resolog.catalog.messaging.event.ProductUnpublished;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, String> kafkaTemplate) {
        var recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate);
        var backoff = new FixedBackOff(1000L, 3);
        return new DefaultErrorHandler(recoverer, backoff);
    }

    @Bean
    public KafkaAdmin.NewTopics createTopics() {
        return new KafkaAdmin.NewTopics(
                TopicBuilder.name(KafkaTopics.topicFor(ProductCreated.class))
                        .partitions(1)
                        .replicas(1)
                        .build(),
                TopicBuilder.name(KafkaTopics.topicFor(ProductSubmittedForPublishing.class))
                        .partitions(1)
                        .replicas(1)
                        .build(),
                TopicBuilder.name(KafkaTopics.topicFor(ProductPublished.class))
                        .partitions(1)
                        .replicas(1)
                        .build(),
                TopicBuilder.name(KafkaTopics.topicFor(ProductRejected.class))
                        .partitions(1)
                        .replicas(1)
                        .build(),
                TopicBuilder.name(KafkaTopics.topicFor(ProductUnpublished.class))
                        .partitions(1)
                        .replicas(1)
                        .build(),
                TopicBuilder.name(KafkaTopics.topicFor(ProductDeleted.class))
                        .partitions(1)
                        .replicas(1)
                        .build(),
                TopicBuilder.name(KafkaTopics.topicFor(ProductSubmissionApproved.class))
                        .partitions(1)
                        .replicas(1)
                        .build(),
                TopicBuilder.name(KafkaTopics.topicFor(ProductSubmissionDeclined.class))
                        .partitions(1)
                        .replicas(1)
                        .build(),
                TopicBuilder.name(KafkaTopics.topicFor(ProductCreated.class) + ".DLT")
                        .partitions(1)
                        .replicas(1)
                        .build(),
                TopicBuilder.name(KafkaTopics.topicFor(ProductSubmittedForPublishing.class) + ".DLT")
                        .partitions(1)
                        .replicas(1)
                        .build(),
                TopicBuilder.name(KafkaTopics.topicFor(ProductPublished.class) + ".DLT")
                        .partitions(1)
                        .replicas(1)
                        .build(),
                TopicBuilder.name(KafkaTopics.topicFor(ProductRejected.class) + ".DLT")
                        .partitions(1)
                        .replicas(1)
                        .build(),
                TopicBuilder.name(KafkaTopics.topicFor(ProductUnpublished.class) + ".DLT")
                        .partitions(1)
                        .replicas(1)
                        .build(),
                TopicBuilder.name(KafkaTopics.topicFor(ProductDeleted.class) + ".DLT")
                        .partitions(1)
                        .replicas(1)
                        .build(),
                TopicBuilder.name(KafkaTopics.topicFor(ProductSubmissionApproved.class) + ".DLT")
                        .partitions(1)
                        .replicas(1)
                        .build(),
                TopicBuilder.name(KafkaTopics.topicFor(ProductSubmissionDeclined.class) + ".DLT")
                        .partitions(1)
                        .replicas(1)
                        .build()
        );
    }

}
