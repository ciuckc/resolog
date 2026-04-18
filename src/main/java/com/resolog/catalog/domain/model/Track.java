package com.resolog.catalog.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = DbSchema.Tracks.TABLE)
public class Track extends Auditable {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, name = DbSchema.Tracks.TRACK_NUMBER)
    private int trackNumber;

    @Column(nullable = false, name = DbSchema.Tracks.DURATION_SECONDS)
    private int durationSeconds;

    @Column(nullable = false, name = DbSchema.Tracks.AUDIO_URL)
    private String audioUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = DbSchema.Tracks.PRODUCT_ID)
    @ToString.Exclude
    private Product product;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = DbSchema.Tracks.TRACK_ARTIST_TABLE,
            joinColumns = @JoinColumn(name = DbSchema.Tracks.ID),
            inverseJoinColumns = @JoinColumn(name = DbSchema.Tracks.ARTIST_ID)
    )
    @ToString.Exclude
    private Set<Artist> trackArtists = new HashSet<>();

    public static Track create(
            @NonNull String title,
            int trackNumber,
            int durationSeconds,
            @NonNull String audioUrl,
            @NonNull Product product) {
        Track track = new Track();
        track.updateTitle(title);
        track.updateTrackNumber(trackNumber);
        track.updateDurationSeconds(durationSeconds);
        track.updateAudioUrl(audioUrl);
        track.product = product;
        return track;
    }

    public void updateTitle(@NonNull String title) {
        if (title.isBlank()) {
            throw new IllegalArgumentException("Track title cannot be blank");
        }
        this.title = title;
    }

    public void updateTrackNumber(int trackNumber) {
        if (trackNumber <= 0) {
            throw new IllegalArgumentException("Track number must be positive");
        }
        this.trackNumber = trackNumber;
    }

    // was thinking to add a check for track duration bigger than 3600 seconds,
    // but maybe we have cases like that (e.g., classical music, noise track)
    public void updateDurationSeconds(int durationSeconds) {
        if (durationSeconds <= 0) {
            throw new IllegalArgumentException("Duration must be positive");
        }
        this.durationSeconds = durationSeconds;
    }

    public void updateAudioUrl(@NonNull String audioUrl) {
        if (audioUrl.isBlank()) {
            throw new IllegalArgumentException("Audio URL cannot be blank");
        }
        this.audioUrl = audioUrl;
    }

    public void addArtist(@NonNull Artist artist) {
        this.trackArtists.add(artist);
    }

    public void removeArtist(@NonNull Artist artist) {
        if (!this.trackArtists.contains(artist)) {
            throw new IllegalArgumentException("Artist does not exist");
        }
        this.trackArtists.remove(artist);
    }

}