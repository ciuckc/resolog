package com.resolog.catalog.api.request;

public record CreateArtistRequest(

        String name,

        String label,

        String biography

) { }
