package com.resolog.catalog.api.request;

import com.resolog.catalog.domain.model.ProductType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateProductRequest(

        ProductType type,

        String title,

        String genre,

        BigDecimal price,

        LocalDate releaseDate,

        String artworkUrl

) { }
