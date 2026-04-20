package com.resolog.catalog.api.request;

public record UpdateArtistRequest(

        String name,

        String label,

        String biography

) { }
