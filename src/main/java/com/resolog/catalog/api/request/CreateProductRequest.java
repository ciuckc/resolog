package com.resolog.catalog.api.request;

import com.resolog.catalog.domain.model.ProductType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record CreateProductRequest(

        ProductType productType,

        String title,

        String genre,

        BigDecimal price,

        LocalDate releaseDate,

        String artworkUrl,

        Set<UUID> artistIds

) { }
