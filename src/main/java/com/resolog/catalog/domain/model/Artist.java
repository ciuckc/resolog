package com.resolog.catalog.domain.model;

import jakarta.persistence.*;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = DbSchema.Artists.TABLE)
public class Artist extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Version
    private Long version;

    private String label;

    private String biography;

    public static Artist create(@NonNull String name) {
        Artist artist = new Artist();
        artist.updateName(name);
        return artist;
    }

    public void updateName(@NonNull String name) {
        if (name.isBlank()) {
            throw new IllegalArgumentException("Artist name cannot be blank");
        }
        this.name = name;
    }

    public void updateLabel(@NonNull String label) {
        if (label.isBlank()) {
            throw new IllegalArgumentException("Artist label cannot be blank");
        }
        this.label = label;
    }

    public void updateBiography(@NonNull String biography) {
        if (biography.isBlank()) {
            throw new IllegalArgumentException("Artist biography cannot be blank");
        }
        this.biography = biography;
    }
}
