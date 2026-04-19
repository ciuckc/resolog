package com.resolog.catalog.domain.repository;

import com.resolog.catalog.domain.model.Product;
import com.resolog.catalog.domain.model.Track;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TrackRepository extends JpaRepository<Track, UUID> {

    List<Track> findByProductId(UUID productId);

    boolean existsByTrackArtistsId(UUID artistId);
}
