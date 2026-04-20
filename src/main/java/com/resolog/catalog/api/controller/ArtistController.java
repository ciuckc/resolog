package com.resolog.catalog.api.controller;

import com.resolog.catalog.api.request.CreateArtistRequest;
import com.resolog.catalog.api.request.UpdateArtistRequest;
import com.resolog.catalog.api.response.GetArtistResponse;
import com.resolog.catalog.api.response.ListArtistsResponse;
import com.resolog.catalog.service.ArtistService;
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
@RequestMapping("/artists")
public class ArtistController {

    private final ArtistService artistService;

    public ArtistController(ArtistService artistService) {
        this.artistService = artistService;
    }

    @Operation(summary = "List artists", description = "Returns all artists.")
    @GetMapping
    public ListArtistsResponse listArtists() {
        return artistService.listArtists();
    }

    @Operation(summary = "Get artist by Id", description = "Returns a single artist. Returns 404 if not found.")
    @GetMapping("/{id}")
    public GetArtistResponse getArtist(@PathVariable UUID id) {
        return artistService.getArtist(id);
    }

    @Operation(summary = "Create artist", description = "Creates a new artist. Name is required.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GetArtistResponse createArtist(@RequestBody CreateArtistRequest request) {
        return artistService.createArtist(request);
    }

    @Operation(summary = "Update artist", description = "Partially updates an artist.")
    @PatchMapping("/{id}")
    public GetArtistResponse updateArtist(@PathVariable UUID id, @RequestBody UpdateArtistRequest request) {
        return artistService.updateArtist(id, request);
    }

    @Operation(summary = "Delete artist", description = "Deletes an artist. " +
            "Returns 409 if the artist is still linked to any product or track.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteArtist(@PathVariable UUID id) {
        artistService.deleteArtist(id);
    }

}
