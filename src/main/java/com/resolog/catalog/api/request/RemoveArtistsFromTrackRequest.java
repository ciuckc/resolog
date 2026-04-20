package com.resolog.catalog.api.request;

import java.util.Set;
import java.util.UUID;

public record RemoveArtistsFromTrackRequest(

        Set<UUID> artistIds

) { }
