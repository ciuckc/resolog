package com.resolog.catalog.service;

import com.resolog.catalog.api.request.CreateArtistRequest;
import com.resolog.catalog.api.request.UpdateArtistRequest;
import com.resolog.catalog.api.response.GetArtistResponse;
import com.resolog.catalog.domain.model.Artist;
import com.resolog.catalog.domain.model.Product;
import com.resolog.catalog.TestFixtures;
import com.resolog.catalog.domain.repository.ArtistRepository;
import com.resolog.catalog.domain.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class ArtistServiceTest {

    @Autowired
    private ArtistService artistService;

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private ProductRepository productRepository;

    private Artist savedArtist;

    @BeforeEach
    void setUp() {
        savedArtist = artistRepository.save(TestFixtures.anArtist());
    }

    @Test
    void createArtist_createsArtist() {
        GetArtistResponse response = artistService.createArtist(TestFixtures.aCreateArtistRequest());

        assertNotNull(response.id());
        assertEquals(TestFixtures.ARTIST_NAME, response.name());
        assertEquals(TestFixtures.ARTIST_LABEL, response.label());
    }

    @Test
    void createArtist_throwsWhenNameIsNull() {
        CreateArtistRequest request = new CreateArtistRequest(null, null, null);
        assertThrows(IllegalArgumentException.class, () -> artistService.createArtist(request));
    }

    @Test
    void getArtist_returnsArtist() {
        GetArtistResponse response = artistService.getArtist(savedArtist.getId());
        assertEquals(savedArtist.getId(), response.id());
        assertEquals(savedArtist.getName(), response.name());
    }

    @Test
    void getArtist_throwsWhenNotFound() {
        assertThrows(NoSuchElementException.class, () -> artistService.getArtist(UUID.randomUUID()));
    }

    @Test
    void updateArtist_updatesNonNullFields() {
        UpdateArtistRequest request = new UpdateArtistRequest(TestFixtures.UPDATED_ARTIST_NAME, null, null);
        GetArtistResponse response = artistService.updateArtist(savedArtist.getId(), request);

        assertEquals(TestFixtures.UPDATED_ARTIST_NAME, response.name());
        assertEquals(savedArtist.getLabel(), response.label());
    }

    @Test
    void listArtists_returnsAllArtists() {
        var response = artistService.listArtists();
        assertNotNull(response.artists());
        assertTrue(response.artists().stream().anyMatch(a -> a.id().equals(savedArtist.getId())));
    }

    @Test
    void deleteArtist_deletesArtist() {
        artistService.deleteArtist(savedArtist.getId());
        assertThrows(NoSuchElementException.class, () -> artistService.getArtist(savedArtist.getId()));
    }

    @Test
    void deleteArtist_throwsWhenLinkedToProduct() {
        Product product = productRepository.save(TestFixtures.aProduct());
        product.addArtist(savedArtist);
        productRepository.save(product);

        assertThrows(IllegalStateException.class, () -> artistService.deleteArtist(savedArtist.getId()));
    }

    @Test
    void deleteArtist_throwsWhenNotFound() {
        assertThrows(NoSuchElementException.class, () -> artistService.deleteArtist(UUID.randomUUID()));
    }

    private void assertTrue(boolean condition) {
        org.junit.jupiter.api.Assertions.assertTrue(condition);
    }
}
