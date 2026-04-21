package com.resolog.catalog.messaging.event;

import java.util.UUID;

public record ProductSubmissionApproved(
        UUID aggregateId
) implements Event {}
