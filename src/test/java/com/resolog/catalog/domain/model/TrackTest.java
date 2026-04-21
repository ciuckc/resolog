package com.resolog.catalog.domain.model;

import com.resolog.catalog.TestFixtures;
import com.resolog.catalog.config.JpaConfig;
import com.resolog.catalog.domain.repository.ArtistRepository;
import com.resolog.catalog.domain.repository.ProductRepository;
import com.resolog.catalog.domain.repository.TrackRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@Import(JpaConfig.class)
public class TrackTest {

    private Track track;
    private Artist artist;

    @Autowired
    private TrackRepository trackRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ArtistRepository artistRepository;

    @BeforeEach
    void setUp() {
        Product product = productRepository.saveAndFlush(TestFixtures.aProduct());
        track = trackRepository.saveAndFlush(TestFixtures.aTrack(product));
        artist = artistRepository.saveAndFlush(TestFixtures.anArtist());
    }

    @Test
    public void create_persistsTrack() {
        assertNotNull(track.getId());
    }

    @Test
    public void updateTitle_throwsWhenNull() {
        assertThrows(NullPointerException.class, () -> track.updateTitle(null));
    }

    @Test
    public void updateTitle_throwsWhenBlank() {
        assertThrows(IllegalArgumentException.class, () -> track.updateTitle(""));
    }

    @Test
    public void updateTrackNumber_throwsWhenZero() {
        assertThrows(IllegalArgumentException.class, () -> track.updateTrackNumber(0));
    }

    @Test
    public void updateDurationSeconds_throwsWhenZero() {
        assertThrows(IllegalArgumentException.class, () -> track.updateDurationSeconds(0));
    }

    @Test
    public void addArtist_linksArtist() {
        track.addArtist(artist);
        Track updated = trackRepository.saveAndFlush(track);
        assertEquals(1, updated.getTrackArtists().size());
    }

    @Test
    public void removeArtist_unlinksArtist() {
        track.addArtist(artist);
        trackRepository.saveAndFlush(track);
        track.removeArtist(artist);
        Track updated = trackRepository.saveAndFlush(track);
        assertEquals(0, updated.getTrackArtists().size());
    }

    @Test
    public void removeArtist_throwsWhenArtistNotLinked() {
        assertThrows(IllegalArgumentException.class, () -> track.removeArtist(artist));
    }
}
