package com.resolog.catalog.api.mapper;

import com.resolog.catalog.api.ProductStatusView;
import com.resolog.catalog.api.response.GetArtistResponse;
import com.resolog.catalog.api.response.GetProductResponse;
import com.resolog.catalog.api.response.GetTrackResponse;
import com.resolog.catalog.domain.model.Artist;
import com.resolog.catalog.domain.model.Product;
import com.resolog.catalog.domain.model.ProductStatus;
import com.resolog.catalog.domain.model.Track;

import java.util.stream.Collectors;

public final class CatalogMapper {

    public static GetProductResponse toDto(Product product) {
        return new GetProductResponse(
                product.getId(),
                product.getType(),
                toDto(product.getStatus()),
                product.getTitle(),
                product.getGenre(),
                product.getReleaseDate(),
                product.getArtworkUrl(),
                product.getPrice(),
                product.getArtists().stream()
                        .map(CatalogMapper::toDto)
                        .collect(Collectors.toSet()),
                product.getVersion()
        );
    }

    public static GetArtistResponse toDto(Artist artist) {
        return new GetArtistResponse(
                artist.getId(),
                artist.getName(),
                artist.getLabel(),
                artist.getBiography(),
                artist.getVersion()
        );
    }

    public static GetTrackResponse toDto(Track track) {
        return new GetTrackResponse(
                track.getId(),
                track.getProduct().getId(),
                track.getTitle(),
                track.getTrackNumber(),
                track.getDurationSeconds(),
                track.getAudioUrl(),
                track.getTrackArtists().stream()
                        .map(CatalogMapper::toDto)
                        .collect(Collectors.toSet()),
                track.getVersion()
        );
    }

    public static ProductStatusView toDto(ProductStatus status) {
        return ProductStatusView.valueOf(status.name());
    }

    private CatalogMapper() { }

}
