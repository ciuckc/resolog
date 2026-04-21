package com.resolog.catalog.service;

import com.resolog.catalog.domain.model.ProcessedEvent;
import com.resolog.catalog.domain.repository.ProcessedEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class ProcessedEventService {

    private final ProcessedEventRepository processedEventRepository;

    public ProcessedEventService(ProcessedEventRepository processedEventRepository) {
        this.processedEventRepository = processedEventRepository;
    }

    public boolean hasBeenProcessed(UUID messageId, Acknowledgment ack) {
        if (processedEventRepository.existsById(messageId)) {
            log.info("Skipping duplicate event {}", messageId);
            ack.acknowledge();
            return true;
        }
        return false;
    }

    public void markAsProcessed(UUID messageId) {
        processedEventRepository.save(ProcessedEvent.create(messageId));
    }

}
