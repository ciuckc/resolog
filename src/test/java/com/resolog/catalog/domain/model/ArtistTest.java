package com.resolog.catalog.domain.model;

import com.resolog.catalog.domain.repository.ArtistRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
public class ArtistTest {

    @Autowired
    private ArtistRepository artistRepository;

    @Test
    public void artistSavedHasFieldsSet() {
        final String name = "Name";
        Artist artist = Artist.create(name);

        Artist savedArtist = artistRepository.saveAndFlush(artist);

        assertThat(savedArtist).isNotNull();
        assertThat(savedArtist.getId()).isNotNull();
        assertThat(savedArtist.getName()).isEqualTo(name);
        assertThat(savedArtist.getLabel()).isNull();
        assertThat(savedArtist.getBiography()).isNull();
    }

    @Test
    public void artistSavedWithNullNameThrowsException() {
        Artist artist = new Artist();

        assertThrows(DataIntegrityViolationException.class, () -> artistRepository.saveAndFlush(artist));
    }


    @Test
    public void artistCanBeFoundById() {
        final String name = "Name";
        Artist artist = Artist.create(name);

        Artist savedArtist = artistRepository.saveAndFlush(artist);
        Artist foundArtist = artistRepository.findById(savedArtist.getId()).orElse(null);

        assertThat(foundArtist).isNotNull();
        assertThat(foundArtist.getId()).isEqualTo(savedArtist.getId());
        assertThat(foundArtist.getName()).isEqualTo(savedArtist.getName());
    }

    @Test
    public void artistCanBeUpdated() {
        final String name = "Name";
        Artist artist = Artist.create(name);

        Artist savedArtist = artistRepository.saveAndFlush(artist);

        assertThat(savedArtist.getName()).isEqualTo(name);

        final String newName = "NewName";
        artist.setName(newName);

        Artist updatedArtist = artistRepository.saveAndFlush(artist);

        assertThat(updatedArtist).isNotNull();
        assertThat(updatedArtist.getId()).isEqualTo(savedArtist.getId());
        assertThat(updatedArtist.getName()).isEqualTo(newName);
    }

    @Test
    public void artistCanBeDeleted() {
        final String name = "Name";
        Artist artist = Artist.create(name);

        Artist savedArtist = artistRepository.saveAndFlush(artist);

        assertThat(savedArtist).isNotNull();
        assertThat(savedArtist.getId()).isNotNull();

        artistRepository.delete(savedArtist);

        Artist deletedArtist = artistRepository.findById(savedArtist.getId()).orElse(null);

        assertThat(deletedArtist).isNull();
    }
}
