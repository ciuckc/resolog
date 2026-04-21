package com.resolog.catalog.messaging.event;

public record TrackPayload(
        int trackNumber,
        String audioUrl
) {}
