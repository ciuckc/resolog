package com.resolog.catalog.service;

import com.resolog.catalog.domain.model.OutboxEvent;
import com.resolog.catalog.domain.repository.OutboxEventRepository;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Service
public class OutboxEventService {

    private final OutboxEventRepository outboxEventRepository;

    private final ObjectMapper objectMapper;

    public  OutboxEventService(OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    public void publish(UUID aggregateId, String eventType, Object payload) {
        String json = this.objectMapper.writeValueAsString(payload);
        outboxEventRepository.save(OutboxEvent.create(aggregateId, eventType, json));
    }
}
