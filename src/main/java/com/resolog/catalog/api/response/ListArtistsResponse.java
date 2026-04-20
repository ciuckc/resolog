package com.resolog.catalog.api.response;

import java.util.List;

public record ListArtistsResponse(

        List<GetArtistResponse> artists

) { }
