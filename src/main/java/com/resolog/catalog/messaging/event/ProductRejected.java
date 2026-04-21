package com.resolog.catalog.messaging.event;

import com.resolog.catalog.domain.model.ProductStatus;
import com.resolog.catalog.domain.model.ProductType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record ProductRejected(
        UUID aggregateId,
        ProductType type,
        ProductStatus status,
        String title,
        String genre,
        LocalDate releaseDate,
        String artworkUrl,
        BigDecimal price,
        String statusReason,
        Set<UUID> artistIds
) implements Event {}
