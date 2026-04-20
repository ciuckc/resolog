package com.resolog.catalog.api.request;

import java.util.Set;
import java.util.UUID;

public record AddArtistsToTrackRequest(

        Set<UUID> artistIds

) { }
