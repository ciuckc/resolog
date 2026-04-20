package com.resolog.catalog.service;

import com.resolog.catalog.api.request.AddArtistsToTrackRequest;
import com.resolog.catalog.api.request.UpdateTrackRequest;
import com.resolog.catalog.api.response.GetTrackResponse;
import com.resolog.catalog.domain.model.Artist;
import com.resolog.catalog.domain.model.Product;
import com.resolog.catalog.TestFixtures;
import com.resolog.catalog.domain.model.Track;
import com.resolog.catalog.domain.repository.ArtistRepository;
import com.resolog.catalog.domain.repository.ProductRepository;
import com.resolog.catalog.domain.repository.TrackRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class TrackServiceTest {

    @Autowired
    private TrackService trackService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private TrackRepository trackRepository;

    private Product savedProduct;
    private Artist savedArtist;
    private Track savedTrack;

    @BeforeEach
    void setUp() {
        savedProduct = productRepository.save(TestFixtures.aProduct());
        savedArtist = artistRepository.save(TestFixtures.anArtist());
        savedTrack = trackRepository.save(TestFixtures.aTrack(savedProduct));
    }

    @Test
    void createTrack_createsTrack() {
        GetTrackResponse response = trackService.createTrack(savedProduct.getId(), TestFixtures.aCreateTrackRequest());

        assertNotNull(response.id());
        assertEquals(TestFixtures.TRACK_TITLE, response.title());
        assertEquals(savedProduct.getId(), response.productId());
    }

    @Test
    void createTrack_throwsWhenProductNotFound() {
        assertThrows(NoSuchElementException.class,
                () -> trackService.createTrack(UUID.randomUUID(), TestFixtures.aCreateTrackRequest()));
    }

    @Test
    void getTrack_returnsTrack() {
        GetTrackResponse response = trackService.getTrack(savedProduct.getId(), savedTrack.getId());
        assertEquals(savedTrack.getId(), response.id());
    }

    @Test
    void getTrack_throwsWhenNotFound() {
        assertThrows(NoSuchElementException.class,
                () -> trackService.getTrack(savedProduct.getId(), UUID.randomUUID()));
    }

    @Test
    void getTrack_throwsWhenTrackBelongsToDifferentProduct() {
        Product otherProduct = productRepository.save(TestFixtures.aProduct());
        assertThrows(NoSuchElementException.class,
                () -> trackService.getTrack(otherProduct.getId(), savedTrack.getId()));
    }

    @Test
    void listTracks_returnsTracksForProduct() {
        var response = trackService.listTracks(savedProduct.getId());
        assertNotNull(response.tracks());
        assertTrue(response.tracks().stream()
                .anyMatch(getTrackResponse -> getTrackResponse.id().equals(savedTrack.getId())));
    }

    @Test
    void listTracks_throwsWhenProductNotFound() {
        assertThrows(NoSuchElementException.class,
                () -> trackService.listTracks(UUID.randomUUID()));
    }

    @Test
    void updateTrack_updatesNonNullFields() {
        UpdateTrackRequest request = new UpdateTrackRequest(TestFixtures.UPDATED_TRACK_TITLE, null, null, null);
        GetTrackResponse response = trackService.updateTrack(savedProduct.getId(), savedTrack.getId(), request);

        assertEquals(TestFixtures.UPDATED_TRACK_TITLE, response.title());
        assertEquals(savedTrack.getTrackNumber(), response.trackNumber());
    }

    @Test
    void createTrack_throwsWhenProductIsNotDraft() {
        savedProduct.submit();
        productRepository.save(savedProduct);

        assertThrows(IllegalStateException.class,
                () -> trackService.createTrack(savedProduct.getId(), TestFixtures.aCreateTrackRequest()));
    }

    @Test
    void updateTrack_throwsWhenProductIsNotDraft() {
        UpdateTrackRequest updateRequest = new UpdateTrackRequest(TestFixtures.UPDATED_TRACK_TITLE, null, null, null);
        savedProduct.submit();
        productRepository.save(savedProduct);

        assertThrows(IllegalStateException.class,
                () -> trackService.updateTrack(savedProduct.getId(), savedTrack.getId(), updateRequest));
    }

    @Test
    void deleteTrack_throwsWhenProductIsNotDraft() {
        savedProduct.submit();
        productRepository.save(savedProduct);

        assertThrows(IllegalStateException.class,
                () -> trackService.deleteTrack(savedProduct.getId(), savedTrack.getId()));
    }

    @Test
    void deleteTrack_deletesTrack() {
        trackService.deleteTrack(savedProduct.getId(), savedTrack.getId());
        assertThrows(NoSuchElementException.class,
                () -> trackService.getTrack(savedProduct.getId(), savedTrack.getId()));
    }

    @Test
    void addArtistsToTrack_linksFeaturedArtists() {
        AddArtistsToTrackRequest request = new AddArtistsToTrackRequest(Set.of(savedArtist.getId()));
        GetTrackResponse response = trackService.addArtistsToTrack(savedProduct.getId(), savedTrack.getId(), request);

        assertEquals(1, response.featuredArtists().size());
    }

    @Test
    void addArtistsToTrack_throwsWhenArtistNotFound() {
        AddArtistsToTrackRequest request = new AddArtistsToTrackRequest(Set.of(UUID.randomUUID()));
        assertThrows(NoSuchElementException.class,
                () -> trackService.addArtistsToTrack(savedProduct.getId(), savedTrack.getId(), request));
    }

    @Test
    void addArtistsToTrack_throwsWhenArtistIdsEmpty() {
        AddArtistsToTrackRequest request = new AddArtistsToTrackRequest(Set.of());
        assertThrows(IllegalArgumentException.class,
                () -> trackService.addArtistsToTrack(savedProduct.getId(), savedTrack.getId(), request));
    }
}
