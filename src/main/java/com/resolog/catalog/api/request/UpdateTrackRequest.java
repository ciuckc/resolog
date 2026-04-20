package com.resolog.catalog.api.request;

public record UpdateTrackRequest(

        String title,

        Integer trackNumber,

        Integer durationSeconds,

        String audioUrl

)  { }
