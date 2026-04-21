package com.resolog.catalog.service;

import com.resolog.catalog.api.ProductStatusView;
import com.resolog.catalog.api.request.AddArtistsToProductRequest;
import com.resolog.catalog.api.request.UpdateProductRequest;
import com.resolog.catalog.api.response.GetProductResponse;
import com.resolog.catalog.domain.model.Artist;
import com.resolog.catalog.domain.model.Product;
import com.resolog.catalog.domain.model.ProductStatus;
import com.resolog.catalog.domain.model.ProductType;
import com.resolog.catalog.TestFixtures;
import com.resolog.catalog.domain.model.OutboxEventStatus;
import com.resolog.catalog.domain.repository.ArtistRepository;
import com.resolog.catalog.domain.repository.OutboxEventRepository;
import com.resolog.catalog.domain.repository.ProductRepository;
import com.resolog.catalog.messaging.event.ProductCreated;
import com.resolog.catalog.messaging.event.ProductDeleted;
import com.resolog.catalog.messaging.event.ProductPublished;
import com.resolog.catalog.messaging.event.ProductRejected;
import com.resolog.catalog.messaging.event.ProductSubmittedForPublishing;
import com.resolog.catalog.messaging.event.ProductUnpublished;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@EmbeddedKafka(partitions = 1)
@Transactional
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    private Artist savedArtist;
    private Product savedProduct;

    @BeforeEach
    void setUp() {
        savedArtist = artistRepository.save(TestFixtures.anArtist());
        savedProduct = productRepository.save(TestFixtures.aProduct());
    }

    @Test
    void createProduct_createsDraftProduct() {
        GetProductResponse response = productService.createProduct(TestFixtures.aCreateProductRequest(Set.of(savedArtist.getId())));

        assertNotNull(response.id());
        assertEquals(ProductStatusView.DRAFT, response.status());
        assertEquals(TestFixtures.PRODUCT_TITLE, response.title());
    }

    @Test
    void getActiveProduct_returnsProduct() {
        GetProductResponse response = productService.getActiveProduct(savedProduct.getId());
        assertEquals(savedProduct.getId(), response.id());
    }

    @Test
    void getActiveProduct_throwsWhenNotFound() {
        assertThrows(NoSuchElementException.class,
                () -> productService.getActiveProduct(java.util.UUID.randomUUID()));
    }

    @Test
    void getActiveProduct_throwsWhenDeleted() {
        savedProduct.submit();
        savedProduct.publish();
        savedProduct.unpublish();
        savedProduct.delete();
        productRepository.save(savedProduct);

        assertThrows(NoSuchElementException.class,
                () -> productService.getActiveProduct(savedProduct.getId()));
    }

    @Test
    void updateProduct_updatesNonNullFields() {
        UpdateProductRequest request = new UpdateProductRequest(
                null, TestFixtures.UPDATED_PRODUCT_TITLE, null, null, null, null);

        GetProductResponse response = productService.updateProduct(savedProduct.getId(), request);

        assertEquals(TestFixtures.UPDATED_PRODUCT_TITLE, response.title());
        assertEquals(TestFixtures.PRODUCT_GENRE, response.genre());
    }

    @Test
    void updateProduct_throwsWhenProductIsNotDraft() {
        UpdateProductRequest updateRequest = new UpdateProductRequest(null, TestFixtures.UPDATED_PRODUCT_TITLE, null, null, null, null);
        savedProduct.submit();
        productRepository.save(savedProduct);

        assertThrows(IllegalStateException.class,
                () -> productService.updateProduct(savedProduct.getId(), updateRequest));
    }

    @Test
    void listActiveProducts_excludesDeletedProducts() {
        savedProduct.submit();
        savedProduct.publish();
        savedProduct.unpublish();
        savedProduct.delete();
        productRepository.save(savedProduct);

        var results = productService.listActiveProducts(null, null);
        assertTrue(results.stream().noneMatch(p -> p.id().equals(savedProduct.getId())));
    }

    @Test
    void listActiveProducts_filtersByStatus() {
        var results = productService.listActiveProducts(ProductStatusView.DRAFT, null);
        assertTrue(results.stream().allMatch(p -> p.status() == ProductStatusView.DRAFT));
    }

    @Test
    void listActiveProducts_filtersByType() {
        var results = productService.listActiveProducts(null, ProductType.SINGLE);
        assertTrue(results.stream().allMatch(p -> p.type() == ProductType.SINGLE));
    }

    @Test
    void publishProduct_transitionsToPublishing() {
        GetProductResponse response = productService.publishProduct(savedProduct.getId());
        assertEquals(ProductStatusView.PUBLISHING, response.status());
    }

    @Test
    void unpublishProduct_transitionsToUnpublished() {
        savedProduct.submit();
        savedProduct.publish();
        productRepository.save(savedProduct);

        GetProductResponse response = productService.unpublishProduct(savedProduct.getId());
        assertEquals(ProductStatusView.UNPUBLISHED, response.status());
    }

    @Test
    void revertProductToDraft_transitionsToDraft() {
        savedProduct.submit();
        savedProduct.publish();
        savedProduct.unpublish();
        productRepository.save(savedProduct);

        GetProductResponse response = productService.revertProductToDraft(savedProduct.getId());
        assertEquals(ProductStatusView.DRAFT, response.status());
    }

    @Test
    void deleteProduct_softDeletesProduct() {
        productService.deleteProduct(savedProduct.getId());

        Product deleted = productRepository.findById(savedProduct.getId()).orElseThrow();
        assertEquals(ProductStatus.DELETED, deleted.getStatus());
    }

    @Test
    void deleteProduct_throwsWhenPublished() {
        savedProduct.submit();
        savedProduct.publish();
        productRepository.save(savedProduct);

        assertThrows(IllegalStateException.class,
                () -> productService.deleteProduct(savedProduct.getId()));
    }

    @Test
    void addArtistsToProduct_linksArtists() {
        AddArtistsToProductRequest request = new AddArtistsToProductRequest(Set.of(savedArtist.getId()));

        GetProductResponse response = productService.addArtistsToProduct(savedProduct.getId(), request.artistIds());

        assertEquals(1, response.artists().size());
    }

    @Test
    void addArtistsToProduct_throwsWhenArtistNotFound() {
        assertThrows(NoSuchElementException.class,
                () -> productService.addArtistsToProduct(savedProduct.getId(), Set.of(java.util.UUID.randomUUID())));
    }

    @Test
    void publishProduct_writesOutboxEvent() {
        productService.publishProduct(savedProduct.getId());

        var events = outboxEventRepository.findByStatus(OutboxEventStatus.PENDING);
        assertTrue(events.stream().anyMatch(e ->
                e.getAggregateId().equals(savedProduct.getId()) &&
                e.getEventType().equals(ProductSubmittedForPublishing.class.getSimpleName())));
    }

    @Test
    void createProduct_writesOutboxEvent() {
        productService.createProduct(TestFixtures.aCreateProductRequest(Set.of(savedArtist.getId())));

        var events = outboxEventRepository.findByStatus(OutboxEventStatus.PENDING);
        assertTrue(events.stream().anyMatch(e ->
                e.getEventType().equals(ProductCreated.class.getSimpleName())));
    }

    @Test
    void deleteProduct_writesOutboxEvent() {
        productService.deleteProduct(savedProduct.getId());

        var events = outboxEventRepository.findByStatus(OutboxEventStatus.PENDING);
        assertTrue(events.stream().anyMatch(e ->
                e.getAggregateId().equals(savedProduct.getId()) &&
                e.getEventType().equals(ProductDeleted.class.getSimpleName())));
    }

    @Test
    void unpublishProduct_writesOutboxEvent() {
        savedProduct.submit();
        savedProduct.publish();
        productRepository.save(savedProduct);

        productService.unpublishProduct(savedProduct.getId());

        var events = outboxEventRepository.findByStatus(OutboxEventStatus.PENDING);
        assertTrue(events.stream().anyMatch(e ->
                e.getAggregateId().equals(savedProduct.getId()) &&
                e.getEventType().equals(ProductUnpublished.class.getSimpleName())));
    }

    @Test
    void confirmPublished_writesOutboxEvent() {
        savedProduct.submit();
        productRepository.save(savedProduct);

        productService.confirmPublished(savedProduct.getId());

        var events = outboxEventRepository.findByStatus(OutboxEventStatus.PENDING);
        assertTrue(events.stream().anyMatch(e ->
                e.getAggregateId().equals(savedProduct.getId()) &&
                e.getEventType().equals(ProductPublished.class.getSimpleName())));
    }

    @Test
    void rejectProduct_writesOutboxEvent() {
        savedProduct.submit();
        productRepository.save(savedProduct);

        productService.rejectProduct(savedProduct.getId(), "Bad content");

        var events = outboxEventRepository.findByStatus(OutboxEventStatus.PENDING);
        assertTrue(events.stream().anyMatch(e ->
                e.getAggregateId().equals(savedProduct.getId()) &&
                e.getEventType().equals(ProductRejected.class.getSimpleName())));
    }

    @Test
    void removeArtistFromProduct_unlinksArtists() {
        savedProduct.addArtist(savedArtist);
        productRepository.save(savedProduct);

        productService.removeArtistFromProduct(savedProduct.getId(), Set.of(savedArtist.getId()));

        Product updated = productRepository.findById(savedProduct.getId()).orElseThrow();
        assertEquals(0, updated.getArtists().size());
    }
}
