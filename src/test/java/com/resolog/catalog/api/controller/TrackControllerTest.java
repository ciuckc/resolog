package com.resolog.catalog.api.controller;

import com.resolog.catalog.TestFixtures;
import com.resolog.catalog.api.request.AddArtistsToTrackRequest;
import com.resolog.catalog.api.request.RemoveArtistsFromTrackRequest;
import com.resolog.catalog.api.request.UpdateTrackRequest;
import com.resolog.catalog.domain.model.Artist;
import com.resolog.catalog.domain.model.Product;
import com.resolog.catalog.domain.model.Track;
import com.resolog.catalog.domain.repository.ArtistRepository;
import com.resolog.catalog.domain.repository.ProductRepository;
import com.resolog.catalog.domain.repository.TrackRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@EmbeddedKafka(partitions = 1)
@Transactional
class TrackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TrackRepository trackRepository;

    @Autowired
    private ArtistRepository artistRepository;

    private Product savedProduct;
    private Track savedTrack;
    private Artist savedArtist;

    @BeforeEach
    void setUp() {
        savedProduct = productRepository.save(TestFixtures.aProduct());
        savedTrack = trackRepository.save(TestFixtures.aTrack(savedProduct));
        savedArtist = artistRepository.save(TestFixtures.anArtist());
    }

    @Test
    void createTrack_returns201() throws Exception {
        mockMvc.perform(post("/products/{productId}/tracks", savedProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TestFixtures.aCreateTrackRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(TestFixtures.TRACK_TITLE));
    }

    @Test
    void getTrack_returns200() throws Exception {
        mockMvc.perform(get("/products/{productId}/tracks/{trackId}", savedProduct.getId(), savedTrack.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedTrack.getId().toString()));
    }

    @Test
    void getTrack_returns404WhenNotFound() throws Exception {
        mockMvc.perform(get("/products/{productId}/tracks/{trackId}", savedProduct.getId(), UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void listTracks_returns200() throws Exception {
        mockMvc.perform(get("/products/{productId}/tracks", savedProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tracks").isArray());
    }

    @Test
    void updateTrack_returns200WithUpdatedTitle() throws Exception {
        var request = new UpdateTrackRequest(TestFixtures.UPDATED_TRACK_TITLE, null, null, null);

        mockMvc.perform(patch("/products/{productId}/tracks/{trackId}", savedProduct.getId(), savedTrack.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(TestFixtures.UPDATED_TRACK_TITLE));
    }

    @Test
    void deleteTrack_returns204() throws Exception {
        mockMvc.perform(delete("/products/{productId}/tracks/{trackId}", savedProduct.getId(), savedTrack.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void addFeaturedArtists_returns200() throws Exception {
        var request = new AddArtistsToTrackRequest(Set.of(savedArtist.getId()));

        mockMvc.perform(post("/products/{productId}/tracks/{trackId}/featured-artists", savedProduct.getId(), savedTrack.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.featuredArtists").isArray());
    }

    @Test
    void removeFeaturedArtists_returns200() throws Exception {
        savedTrack.addArtist(savedArtist);
        trackRepository.save(savedTrack);

        var request = new RemoveArtistsFromTrackRequest(Set.of(savedArtist.getId()));

        mockMvc.perform(post("/products/{productId}/tracks/{trackId}/featured-artists/remove", savedProduct.getId(), savedTrack.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.featuredArtists").isArray());
    }
}
