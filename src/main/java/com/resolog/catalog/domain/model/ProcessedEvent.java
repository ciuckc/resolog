package com.resolog.catalog.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processed_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ProcessedEvent {

    @Id
    private UUID eventId;

    @Column(nullable = false)
    private Instant processedAt;

    public static ProcessedEvent create(UUID eventId) {
        return new ProcessedEvent(eventId, Instant.now());
    }
}
