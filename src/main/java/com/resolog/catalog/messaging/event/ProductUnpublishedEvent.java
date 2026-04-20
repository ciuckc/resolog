package com.resolog.catalog.messaging.event;

import java.util.UUID;

public record ProductUnpublishedEvent(
        UUID productId
) {
}
