package com.resolog.catalog.service;

import com.resolog.catalog.api.mapper.CatalogMapper;
import com.resolog.catalog.api.request.CreateArtistRequest;
import com.resolog.catalog.api.request.UpdateArtistRequest;
import com.resolog.catalog.api.response.GetArtistResponse;
import com.resolog.catalog.api.response.ListArtistsResponse;
import com.resolog.catalog.domain.model.Artist;
import com.resolog.catalog.domain.repository.ArtistRepository;
import com.resolog.catalog.domain.repository.ProductRepository;
import com.resolog.catalog.domain.repository.TrackRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class ArtistService {

    private final ArtistRepository artistRepository;

    private final ProductRepository productRepository;

    private final TrackRepository trackRepository;

    public ArtistService(ArtistRepository artistRepository, ProductRepository productRepository, TrackRepository trackRepository) {
        this.artistRepository = artistRepository;
        this.productRepository = productRepository;
        this.trackRepository = trackRepository;
    }

    public ListArtistsResponse listArtists() {
        return new ListArtistsResponse(artistRepository.findAll().stream()
                .map(CatalogMapper::toDto)
                .toList());
    }

    public GetArtistResponse getArtist(UUID id) {
        return CatalogMapper.toDto(findArtist(id));
    }

    public GetArtistResponse createArtist(CreateArtistRequest request) {
        if (request.name() == null || request.name().isEmpty()) {
            throw new IllegalArgumentException("Artist name cannot be empty");
        }

        Artist artist = Artist.create(request.name());

        if (request.label() != null) {
            artist.updateLabel(request.label());
        }
        if (request.biography() != null) {
            artist.updateBiography(request.biography());
        }

        return CatalogMapper.toDto(artistRepository.save(artist));
    }

    public GetArtistResponse updateArtist(UUID id, UpdateArtistRequest request) {
        Artist artist = findArtist(id);

        if (request.name() != null) {
            artist.updateName(request.name());
        }
        if (request.label() != null) {
            artist.updateLabel(request.label());
        }
        if (request.biography() != null) {
            artist.updateBiography(request.biography());
        }

        return CatalogMapper.toDto(artistRepository.save(artist));
    }

    public void deleteArtist(UUID id) {
        Artist artist = findArtist(id);

        if (productRepository.existsByArtistsId(id) || trackRepository.existsByTrackArtistsId(id)) {
            throw new IllegalStateException("Cannot delete artist that is linked to products or tracks");
        }

        artistRepository.delete(artist);
    }

    private Artist findArtist(UUID id) {
        return artistRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Artist not found"));
    }
}
