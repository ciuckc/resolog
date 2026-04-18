package com.resolog.catalog.domain.model;

import lombok.NonNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TestFixtures {
    public static final String ARTIST_NAME = "Test Artist";
    public static final String ARTIST_LABEL = "Test Label";
    public static final String ARTIST_BIOGRAPHY = "Test Biography";

    public static final String PRODUCT_TITLE = "Test Title";
    public static final String PRODUCT_GENRE = "Test Genre";
    public static final BigDecimal PRODUCT_PRICE = BigDecimal.valueOf(1.0);
    public static final LocalDate PRODUCT_RELEASE_DATE = LocalDate.of(2023, 1, 1);

    public static final String TRACK_TITLE = "BEST TRACK EVER";
    public static final int TRACK_NUMBER = 1;
    public static final int TRACK_DURATION = 1;
    public static String TRACK_AUDIO_URL = "S3";

    public static Artist anArtist() {
        return Artist.create(ARTIST_NAME);
    }

    public static Product aProduct() {
        return Product.create(
                ProductType.SINGLE,
                PRODUCT_TITLE,
                PRODUCT_GENRE,
                PRODUCT_PRICE,
                PRODUCT_RELEASE_DATE
                );
    }

    public static Track aTrack(@NonNull Product product) {
        return Track.create(
                TRACK_TITLE,
                TRACK_NUMBER,
                TRACK_DURATION,
                TRACK_AUDIO_URL,
                product
        );
    }
}
