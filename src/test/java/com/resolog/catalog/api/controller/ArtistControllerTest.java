package com.resolog.catalog.api.controller;

import com.resolog.catalog.TestFixtures;
import com.resolog.catalog.api.request.UpdateArtistRequest;
import com.resolog.catalog.domain.model.Artist;
import com.resolog.catalog.domain.repository.ArtistRepository;
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
class ArtistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ArtistRepository artistRepository;

    private Artist savedArtist;

    @BeforeEach
    void setUp() {
        savedArtist = artistRepository.save(TestFixtures.anArtist());
    }

    @Test
    void createArtist_returns201() throws Exception {
        mockMvc.perform(post("/artists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TestFixtures.aCreateArtistRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(TestFixtures.ARTIST_NAME));
    }

    @Test
    void getArtist_returns200() throws Exception {
        mockMvc.perform(get("/artists/{id}", savedArtist.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedArtist.getId().toString()));
    }

    @Test
    void getArtist_returns404WhenNotFound() throws Exception {
        mockMvc.perform(get("/artists/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void listArtists_returns200() throws Exception {
        mockMvc.perform(get("/artists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.artists").isArray());
    }

    @Test
    void updateArtist_returns200WithUpdatedName() throws Exception {
        var request = new UpdateArtistRequest(TestFixtures.UPDATED_ARTIST_NAME, null, null);

        mockMvc.perform(patch("/artists/{id}", savedArtist.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(TestFixtures.UPDATED_ARTIST_NAME));
    }

    @Test
    void deleteArtist_returns204() throws Exception {
        mockMvc.perform(delete("/artists/{id}", savedArtist.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteArtist_returns404WhenNotFound() throws Exception {
        mockMvc.perform(delete("/artists/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}
