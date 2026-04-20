package com.resolog.catalog.api.response;

import java.util.UUID;

public record GetArtistResponse (

    UUID id,

    String name,

    String label,

    String biography,

    Long version

) { }
