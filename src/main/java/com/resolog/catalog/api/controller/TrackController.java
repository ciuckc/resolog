package com.resolog.catalog.api.controller;

import com.resolog.catalog.api.request.AddArtistsToTrackRequest;
import com.resolog.catalog.api.request.CreateTrackRequest;
import com.resolog.catalog.api.request.RemoveArtistsFromTrackRequest;
import com.resolog.catalog.api.request.UpdateTrackRequest;
import com.resolog.catalog.api.response.GetTrackResponse;
import com.resolog.catalog.api.response.ListTracksResponse;
import com.resolog.catalog.service.TrackService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/products/{productId}/tracks")
public class TrackController {

    private final TrackService trackService;

    public TrackController(TrackService trackService) {
        this.trackService = trackService;
    }

    @Operation(summary = "List tracks", description = "Returns all tracks for a product. " +
            "Returns 404 if the product doesn't exist.")
    @GetMapping
    public ListTracksResponse listTracks(@PathVariable UUID productId) {
        return trackService.listTracks(productId);
    }

    @Operation(summary = "Get track by ID", description = "Returns a single track for a product.")
    @GetMapping("/{trackId}")
    public GetTrackResponse getTrack(@PathVariable UUID productId, @PathVariable UUID trackId) {
        return trackService.getTrack(productId, trackId);
    }

    @Operation(summary = "Create track", description = "Creates a new track under the given product.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GetTrackResponse createTrack(@PathVariable UUID productId, @RequestBody CreateTrackRequest request) {
        return trackService.createTrack(productId, request);
    }

    @Operation(summary = "Update track", description = "Partially updates a track. Only NonNull fields are applied.")
    @PatchMapping("/{trackId}")
    public GetTrackResponse updateTrack(
            @PathVariable UUID productId,
            @PathVariable UUID trackId,
            @RequestBody UpdateTrackRequest request) {
        return trackService.updateTrack(productId, trackId, request);
    }

    @Operation(summary = "Delete track", description = "Deletes a track from a product.")
    @DeleteMapping("/{trackId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTrack(@PathVariable UUID productId, @PathVariable UUID trackId) {
        trackService.deleteTrack(productId, trackId);
    }

    @Operation(
            summary = "Add featured artists to track",
            description = "Links one or more artists as featured artists on a track. " +
                    "If any artist Id is invalid the operation rolls back.")
    @PostMapping("/{trackId}/featured-artists")
    public GetTrackResponse addArtistsToTrack(
            @PathVariable UUID productId,
            @PathVariable UUID trackId,
            @RequestBody AddArtistsToTrackRequest request) {
        return trackService.addArtistsToTrack(productId, trackId, request);
    }

    @Operation(
            summary = "Remove featured artists from track",
            description = "Unlinks one or more featured artists from a track. " +
                    "If any artist Id is invalid the operation rolls back.")
    @PostMapping("/{trackId}/featured-artists/remove")
    public GetTrackResponse removeArtistsFromTrack(
            @PathVariable UUID productId,
            @PathVariable UUID trackId,
            @RequestBody RemoveArtistsFromTrackRequest request) {
        return trackService.removeArtistsFromTrack(productId, trackId, request);
    }
}
