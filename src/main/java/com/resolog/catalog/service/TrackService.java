package com.resolog.catalog.service;

import com.resolog.catalog.api.mapper.CatalogMapper;
import com.resolog.catalog.api.request.AddArtistsToTrackRequest;
import com.resolog.catalog.api.request.CreateTrackRequest;
import com.resolog.catalog.api.request.RemoveArtistsFromTrackRequest;
import com.resolog.catalog.api.request.UpdateTrackRequest;
import com.resolog.catalog.api.response.GetTrackResponse;
import com.resolog.catalog.api.response.ListTracksResponse;
import com.resolog.catalog.domain.model.Product;
import com.resolog.catalog.domain.model.ProductStatus;
import com.resolog.catalog.domain.model.Track;
import com.resolog.catalog.domain.repository.ArtistRepository;
import com.resolog.catalog.domain.repository.ProductRepository;
import com.resolog.catalog.domain.repository.TrackRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class TrackService {

    private final ArtistRepository artistRepository;
    private final TrackRepository trackRepository;
    private final ProductRepository productRepository;

    public TrackService(ArtistRepository artistRepository, ProductRepository productRepository, TrackRepository trackRepository) {
        this.artistRepository = artistRepository;
        this.productRepository = productRepository;
        this.trackRepository = trackRepository;
    }

    public ListTracksResponse listTracks(UUID productId) {
        getActiveProduct(productId);

        return new ListTracksResponse(trackRepository.findByProductId(productId).stream()
                .map(CatalogMapper::toDto)
                .toList());
    }

    public GetTrackResponse getTrack(UUID productId, UUID trackId) {
        return CatalogMapper.toDto(findTrack(productId, trackId));
    }

    public GetTrackResponse createTrack(UUID productId, CreateTrackRequest request) {
        Product product = getActiveProduct(productId);
        ensureProductEditable(product);

        return CatalogMapper.toDto(trackRepository.save(
                Track.create(
                        request.title(),
                        request.trackNumber(),
                        request.durationSeconds(),
                        request.audioUrl(),
                        product)));
    }

    public GetTrackResponse updateTrack(UUID productId, UUID trackId, UpdateTrackRequest request) {
        ensureProductEditable(getActiveProduct(productId));
        Track track = findTrack(productId, trackId);

        if (request.title() != null) {
            track.updateTitle(request.title());
        }
        if (request.trackNumber() != null) {
            track.updateTrackNumber(request.trackNumber());
        }
        if (request.durationSeconds() != null) {
            track.updateDurationSeconds(request.durationSeconds());
        }
        if (request.audioUrl() != null) {
            track.updateAudioUrl(request.audioUrl());
        }

        return CatalogMapper.toDto(trackRepository.save(track));
    }

    public void deleteTrack(UUID productId, UUID trackId) {
        ensureProductEditable(getActiveProduct(productId));
        Track track = findTrack(productId, trackId);
        trackRepository.delete(track);
    }

    @Transactional
    public GetTrackResponse addArtistsToTrack(UUID productId, UUID trackId, AddArtistsToTrackRequest request) {
        ensureProductEditable(getActiveProduct(productId));

        if (request.artistIds() == null || request.artistIds().isEmpty()) {
            throw new IllegalArgumentException("Artist Id cannot be empty");
        }

        Track track = findTrack(productId, trackId);

        request.artistIds().stream()
                .map(artistId -> artistRepository.findById(artistId)
                        .orElseThrow(() ->
                                new NoSuchElementException("No artist with id " + artistId)))
                .forEach(track::addArtist);

        return CatalogMapper.toDto(trackRepository.save(track));
    }

    @Transactional
    public GetTrackResponse removeArtistsFromTrack(UUID productId, UUID trackId, RemoveArtistsFromTrackRequest request) {
        ensureProductEditable(getActiveProduct(productId));

        if (request.artistIds() == null || request.artistIds().isEmpty()) {
            throw new IllegalArgumentException("Artist Ids cannot be empty");
        }

        Track track = findTrack(productId, trackId);

        request.artistIds().stream()
                .map(artistId -> artistRepository.findById(artistId)
                        .orElseThrow(() ->
                                new NoSuchElementException("No artist with id " + artistId)))
                .forEach(track::removeArtist);

        return CatalogMapper.toDto(trackRepository.save(track));
    }

    private Product getActiveProduct(UUID productId) {
        return productRepository.findById(productId)
                .filter(product -> product.getStatus() != ProductStatus.DELETED)
                .orElseThrow(() ->
                        new NoSuchElementException("Product not found"));
    }

    private Track findTrack(UUID productId, UUID trackId) {
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() ->
                        new NoSuchElementException("Track not found"));
        if (!track.getProduct().getId().equals(productId)) {
            throw new NoSuchElementException("Track not found");
        }
        return track;
    }

    private void ensureProductEditable(Product product) {
        if (product.getStatus() != ProductStatus.DRAFT) {
            throw new IllegalStateException("Cannot modify tracks while product is not in DRAFT status");
        }
    }

}
