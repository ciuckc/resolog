package com.resolog.catalog.api.response;

import com.resolog.catalog.api.ProductStatusView;
import com.resolog.catalog.domain.model.ProductType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record GetProductResponse (

    UUID id,

    ProductType type,

    ProductStatusView status,

    String title,

    String genre,

    LocalDate releaseDate,

    String artworkUrl,

    BigDecimal price,

    Set<GetArtistResponse> artists,

    Long version

) { }
