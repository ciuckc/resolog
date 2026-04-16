package com.resolog.catalog.domain.repository;

import com.resolog.catalog.domain.model.Artist;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ArtistRepository extends JpaRepository<Artist, UUID> {
}
