package com.resolog.catalog.api.response;

import java.util.List;

public record ListTracksResponse(

        List<GetTrackResponse> tracks

) { }
