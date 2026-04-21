package com.resolog.catalog.messaging.event;

import java.util.UUID;

public record ProductSubmissionDeclined(
        UUID aggregateId,
        String reason
) implements Event {}
