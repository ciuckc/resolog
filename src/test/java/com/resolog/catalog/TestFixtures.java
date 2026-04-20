package com.resolog.catalog;

import com.resolog.catalog.api.request.CreateArtistRequest;
import com.resolog.catalog.api.request.CreateProductRequest;
import com.resolog.catalog.api.request.CreateTrackRequest;
import com.resolog.catalog.domain.model.Artist;
import com.resolog.catalog.domain.model.Product;
import com.resolog.catalog.domain.model.ProductType;
import com.resolog.catalog.domain.model.Track;
import lombok.NonNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TestFixtures {
    public static final String ARTIST_NAME = "Test Artist";
    public static final String ARTIST_LABEL = "Test Label";
    public static final String ARTIST_BIOGRAPHY = "Test Biography";

    public static final ProductType PRODUCT_TYPE = ProductType.SINGLE;
    public static final String PRODUCT_TITLE = "Test Title";
    public static final String PRODUCT_GENRE = "Test Genre";
    public static final BigDecimal PRODUCT_PRICE = BigDecimal.valueOf(1.0);
    public static final LocalDate PRODUCT_RELEASE_DATE = LocalDate.of(2023, 1, 1);
    public static final String PRODUCT_ARTWORK_URL = "AWS or GCP or home server";

    public static final String TRACK_TITLE = "BEST TRACK EVER";
    public static final int TRACK_NUMBER = 1;
    public static final int TRACK_DURATION = 1;
    public static String TRACK_AUDIO_URL = "S3";

    public static final String UPDATED_PRODUCT_TITLE = PRODUCT_TITLE + " Updated";
    public static final String UPDATED_ARTIST_NAME = ARTIST_NAME + " Updated";
    public static final String UPDATED_TRACK_TITLE = TRACK_TITLE + " Updated";

    public static Artist anArtist() {
        return Artist.create(ARTIST_NAME);
    }

    public static Product aProduct() {
        return Product.create(
                PRODUCT_TYPE,
                PRODUCT_TITLE,
                PRODUCT_GENRE,
                PRODUCT_PRICE,
                PRODUCT_RELEASE_DATE
                );
    }

    public static CreateProductRequest aCreateProductRequest() {
        return new CreateProductRequest(
                PRODUCT_TYPE,
                PRODUCT_TITLE,
                PRODUCT_GENRE,
                PRODUCT_PRICE,
                PRODUCT_RELEASE_DATE,
                PRODUCT_ARTWORK_URL
        );
    }

    public static CreateArtistRequest aCreateArtistRequest() {
        return new CreateArtistRequest(ARTIST_NAME, ARTIST_LABEL, ARTIST_BIOGRAPHY);
    }

    public static CreateTrackRequest aCreateTrackRequest() {
        return new CreateTrackRequest(TRACK_TITLE, TRACK_NUMBER, TRACK_DURATION, TRACK_AUDIO_URL);
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
