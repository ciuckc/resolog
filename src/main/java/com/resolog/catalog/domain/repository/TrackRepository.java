package com.resolog.catalog.domain.repository;

import com.resolog.catalog.domain.model.Track;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TrackRepository extends JpaRepository<Track, UUID> {
}
