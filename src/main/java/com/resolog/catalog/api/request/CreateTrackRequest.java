package com.resolog.catalog.api.request;

public record CreateTrackRequest(

        String title,

        int trackNumber,

        int durationSeconds,

        String audioUrl

) { }
