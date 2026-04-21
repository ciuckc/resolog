package com.resolog.catalog.api.controller;

import com.resolog.catalog.TestFixtures;
import com.resolog.catalog.domain.model.Artist;
import com.resolog.catalog.domain.model.Product;
import com.resolog.catalog.domain.repository.ArtistRepository;
import com.resolog.catalog.domain.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import com.resolog.catalog.api.request.UpdateProductRequest;
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
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private ProductRepository productRepository;

    private Artist savedArtist;
    private Product savedProduct;

    @BeforeEach
    void setUp() {
        savedArtist = artistRepository.save(TestFixtures.anArtist());
        savedProduct = productRepository.save(TestFixtures.aProduct());
    }

    @Test
    void createProduct_returns201WithDraftStatus() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TestFixtures.aCreateProductRequest(Set.of(savedArtist.getId())))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.title").value(TestFixtures.PRODUCT_TITLE));
    }

    @Test
    void getProduct_returns200() throws Exception {
        mockMvc.perform(get("/products/{id}", savedProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedProduct.getId().toString()));
    }

    @Test
    void getProduct_returns404WhenNotFound() throws Exception {
        mockMvc.perform(get("/products/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void listProducts_returns200() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray());
    }

    @Test
    void updateProduct_returns200WithUpdatedTitle() throws Exception {
        var request = new UpdateProductRequest(
                null, TestFixtures.UPDATED_PRODUCT_TITLE, null, null, null, null);

        mockMvc.perform(patch("/products/{id}", savedProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(TestFixtures.UPDATED_PRODUCT_TITLE));
    }

    @Test
    void deleteProduct_returns204() throws Exception {
        mockMvc.perform(delete("/products/{id}", savedProduct.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteProduct_returns404WhenNotFound() throws Exception {
        mockMvc.perform(delete("/products/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void publishProduct_returns200WithPublishingStatus() throws Exception {
        mockMvc.perform(post("/products/{id}/publish", savedProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHING"));
    }

    @Test
    void unpublishProduct_returns200WithUnpublishedStatus() throws Exception {
        savedProduct.submit();
        savedProduct.publish();
        productRepository.save(savedProduct);

        mockMvc.perform(post("/products/{id}/unpublish", savedProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UNPUBLISHED"));
    }

    @Test
    void unpublishProduct_returns409WhenNotPublished() throws Exception {
        mockMvc.perform(post("/products/{id}/unpublish", savedProduct.getId()))
                .andExpect(status().isConflict());
    }

    @Test
    void revertProductToDraft_returns200WithDraftStatus() throws Exception {
        savedProduct.submit();
        savedProduct.publish();
        savedProduct.unpublish();
        productRepository.save(savedProduct);

        mockMvc.perform(post("/products/{id}/revert", savedProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }
}
