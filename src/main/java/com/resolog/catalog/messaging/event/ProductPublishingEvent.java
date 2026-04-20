package com.resolog.catalog.messaging.event;

import java.util.UUID;

public record ProductPublishingEvent(
        UUID productId
) { }
