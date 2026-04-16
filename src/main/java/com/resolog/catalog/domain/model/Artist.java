package com.resolog.catalog.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.UUID;

@Entity
@Table(name = "artists")
@Data
@NoArgsConstructor
public class Artist {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private String name;
    private String label;
    private String biography;

    public static Artist create(@NonNull String name) {
        Artist artist = new Artist();
        artist.name = name;
        return artist;
    }
}
