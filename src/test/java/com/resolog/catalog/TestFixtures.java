package com.resolog.catalog;

import com.resolog.catalog.api.request.CreateArtistRequest;
import com.resolog.catalog.api.request.CreateProductRequest;
import com.resolog.catalog.api.request.CreateTrackRequest;
import com.resolog.catalog.domain.model.Artist;
import com.resolog.catalog.domain.model.Product;
import com.resolog.catalog.domain.model.ProductStatus;
import com.resolog.catalog.domain.model.ProductType;
import com.resolog.catalog.domain.model.Track;
import com.resolog.catalog.messaging.event.ProductSubmittedForPublishing;
import com.resolog.catalog.messaging.event.TrackPayload;
import lombok.NonNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class TestFixtures {
    public static final String ARTIST_NAME = "Test Artist";
    public static final String ARTIST_LABEL = "Test Label";
    public static final String ARTIST_BIOGRAPHY = "Test Biography";

    public static final ProductType PRODUCT_TYPE = ProductType.SINGLE;
    public static final String PRODUCT_TITLE = "Test Title";
    public static final String PRODUCT_GENRE = "Test Genre";
    public static final BigDecimal PRODUCT_PRICE = BigDecimal.valueOf(1.0);
    public static final LocalDate PRODUCT_RELEASE_DATE = LocalDate.of(2023, 1, 1);
    public static final String PRODUCT_ARTWORK_URL = "https://example.com/art.jpg";

    public static final String TRACK_TITLE = "BEST TRACK EVER";
    public static final int TRACK_NUMBER = 1;
    public static final int TRACK_DURATION = 1;
    public static String TRACK_AUDIO_URL = "https://example.com/track.mp3";

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

    public static CreateProductRequest aCreateProductRequest(Set<UUID> artistIds) {
        return new CreateProductRequest(
                PRODUCT_TYPE,
                PRODUCT_TITLE,
                PRODUCT_GENRE,
                PRODUCT_PRICE,
                PRODUCT_RELEASE_DATE,
                PRODUCT_ARTWORK_URL,
                artistIds
        );
    }

    public static CreateArtistRequest aCreateArtistRequest() {
        return new CreateArtistRequest(ARTIST_NAME, ARTIST_LABEL, ARTIST_BIOGRAPHY);
    }

    public static CreateTrackRequest aCreateTrackRequest() {
        return new CreateTrackRequest(TRACK_TITLE, TRACK_NUMBER, TRACK_DURATION, TRACK_AUDIO_URL);
    }

    public static TrackPayload aTrackPayload(int trackNumber) {
        return new TrackPayload(trackNumber, TRACK_AUDIO_URL);
    }

    public static ProductSubmittedForPublishing aProductSubmittedForPublishing(UUID aggregateId, List<TrackPayload> tracks) {
        return new ProductSubmittedForPublishing(
                aggregateId,
                PRODUCT_TYPE,
                ProductStatus.DRAFT,
                PRODUCT_TITLE,
                PRODUCT_GENRE,
                PRODUCT_RELEASE_DATE,
                PRODUCT_ARTWORK_URL,
                PRODUCT_PRICE,
                Set.of(),
                tracks);
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
