package com.resolog.catalog.api.response;

import java.util.Set;
import java.util.UUID;

public record GetTrackResponse(

        UUID id,

        UUID productId,

        String title,

        int trackNumber,

        int durationSeconds,

        String audioUrl,

        Set<GetArtistResponse> featuredArtists,

        Long version

) { }
