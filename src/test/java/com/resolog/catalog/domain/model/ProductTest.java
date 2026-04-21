package com.resolog.catalog.domain.model;

import com.resolog.catalog.TestFixtures;
import com.resolog.catalog.config.JpaConfig;
import com.resolog.catalog.domain.repository.ArtistRepository;
import com.resolog.catalog.domain.repository.ProductRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@Import(JpaConfig.class)
public class ProductTest {

    private Product product;
    private Product savedProduct;
    private Artist artist;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ArtistRepository artistRepository;

    @BeforeEach
    protected void setUp() {
        product = TestFixtures.aProduct();
        savedProduct = productRepository.saveAndFlush(product);
        artist = artistRepository.saveAndFlush(TestFixtures.anArtist());
    }

    @Test
    public void create_throwsWhenArgsAreNull() {
        assertThrows(NullPointerException.class, () -> product = Product.create(null, null, null, null, null));
    }

    @Test
    public void create_persistsProduct() {
        assertNotNull(savedProduct);
        assertEquals(product, savedProduct);
    }

    @Test
    public void updateType_updatesType() {
        savedProduct.updateType(ProductType.ALBUM);
        final Product updatedProduct = productRepository.saveAndFlush(savedProduct);

        assertNotNull(updatedProduct);
        assertEquals(ProductType.ALBUM, updatedProduct.getType());
    }

    @Test
    public void updateTitle_throwsWhenEmpty() {
        assertThrows(IllegalArgumentException.class, () -> savedProduct.updateTitle(""));
    }

    @Test
    public void updateTitle_throwsWhenBlank() {
        assertThrows(IllegalArgumentException.class, () -> savedProduct.updateTitle("   \n"));
    }

    @Test
    public void updateGenre_throwsWhenEmpty() {
        assertThrows(IllegalArgumentException.class, () -> savedProduct.updateGenre(""));
    }

    @Test
    public void updateGenre_throwsWhenBlank() {
        assertThrows(IllegalArgumentException.class, () -> savedProduct.updateGenre("   \n"));
    }

    @Test
    public void updatePrice_throwsWhenNegative() {
        assertThrows(IllegalArgumentException.class, () -> savedProduct.updatePrice(BigDecimal.valueOf(-10.0)));
    }

    @Test
    public void updateReleaseDate_throwsWhenNull() {
        assertThrows(NullPointerException.class, () -> savedProduct.updateReleaseDate(null));
    }

    @Test
    public void lifecycle_completesFullTransitionSequence() {
        assertDoesNotThrow(() -> savedProduct.redraft());
        assertDoesNotThrow(() -> savedProduct.submit());
        assertDoesNotThrow(() -> savedProduct.publish());
        assertDoesNotThrow(() -> savedProduct.unpublish());
        assertDoesNotThrow(() -> savedProduct.redraft());
    }

    @Test
    public void create_initialStatusIsDraft() {
        assertEquals(ProductStatus.DRAFT, savedProduct.getStatus());
    }

    @Test
    public void redraft_succeedsWhenDraft() {
        assertDoesNotThrow(() -> savedProduct.redraft());
    }

    @Test
    public void redraft_throwsWhenPublished() {
        transitionStatusTo(savedProduct, ProductStatus.PUBLISHED);
        assertThrows(IllegalStateException.class, () -> savedProduct.redraft());
    }

    @Test
    public void redraft_throwsWhenDeleted() {
        transitionStatusTo(savedProduct, ProductStatus.DELETED);
        assertThrows(IllegalStateException.class, () -> savedProduct.redraft());
    }

    @Test
    public void submit_succeedsWhenAlreadyPublishing() {
        transitionStatusTo(savedProduct, ProductStatus.PUBLISHING);
        assertDoesNotThrow(() -> savedProduct.submit());
    }

    @Test
    public void submit_throwsWhenPublished() {
        transitionStatusTo(savedProduct, ProductStatus.PUBLISHED);
        assertThrows(IllegalStateException.class, () -> savedProduct.submit());
    }

    @Test
    public void submit_throwsWhenDeleted() {
        transitionStatusTo(savedProduct, ProductStatus.DELETED);
        assertThrows(IllegalStateException.class, () -> savedProduct.submit());
    }

    @Test
    public void publish_succeedsWhenAlreadyPublished() {
        transitionStatusTo(savedProduct, ProductStatus.PUBLISHED);
        assertDoesNotThrow(() -> savedProduct.publish());
    }

    @Test
    public void publish_throwsWhenDraft() {
        assertThrows(IllegalStateException.class, () -> savedProduct.publish());
    }

    @Test
    public void publish_throwsWhenUnpublished() {
        transitionStatusTo(savedProduct, ProductStatus.UNPUBLISHED);
        assertThrows(IllegalStateException.class, () -> savedProduct.publish());
    }

    @Test
    public void publish_throwsWhenDeleted() {
        transitionStatusTo(savedProduct, ProductStatus.DELETED);
        assertThrows(IllegalStateException.class, () -> savedProduct.publish());
    }

    @Test
    public void delete_throwsWhenPublished() {
        transitionStatusTo(savedProduct, ProductStatus.PUBLISHED);
        assertThrows(IllegalStateException.class, () -> savedProduct.delete());
    }

    @Test
    public void delete_throwsWhenPublishing() {
        transitionStatusTo(savedProduct, ProductStatus.PUBLISHING);
        assertThrows(IllegalStateException.class, () -> savedProduct.delete());
    }

    @Test
    public void findById_returnsProduct() {
        Product foundProduct = productRepository.findById(savedProduct.getId()).orElse(null);
        assertNotNull(foundProduct);
        assertEquals(savedProduct.getId(), foundProduct.getId());
        assertEquals(savedProduct.getTitle(), foundProduct.getTitle());
        assertEquals(savedProduct.getGenre(), foundProduct.getGenre());
        assertEquals(savedProduct.getPrice(), foundProduct.getPrice());
        assertEquals(savedProduct.getStatus(), foundProduct.getStatus());
    }

    @Test
    public void reject_throwsWhenNotPublishing() {
        assertThrows(IllegalStateException.class, () -> product.reject("hello"));
    }

    @Test
    public void reject_throwsWhenReasonIsBlank() {
        transitionStatusTo(savedProduct, ProductStatus.PUBLISHING);
        assertThrows(IllegalArgumentException.class, () -> product.reject(""));
    }

    @Test
    public void reject_succeedsWhenPublishing() {
        transitionStatusTo(savedProduct, ProductStatus.PUBLISHING);
        assertDoesNotThrow(() -> savedProduct.reject("hello"));
    }

    @Test
    public void addArtist_linksArtist() {
        savedProduct.addArtist(artist);
        Product updated = productRepository.saveAndFlush(savedProduct);
        assertEquals(1, updated.getArtists().size());
    }

    @Test
    public void removeArtist_unlinksArtist() {
        savedProduct.addArtist(artist);
        productRepository.saveAndFlush(savedProduct);
        savedProduct.removeArtist(artist);
        Product updated = productRepository.saveAndFlush(savedProduct);
        assertEquals(0, updated.getArtists().size());
    }

    @Test
    public void removeArtist_throwsWhenArtistNotLinked() {
        assertThrows(IllegalArgumentException.class, () -> savedProduct.removeArtist(artist));
    }

    private void transitionStatusTo(Product product, ProductStatus type) {
        switch (type) {
            case PUBLISHING -> product.submit();
            case PUBLISHED -> {
                product.submit();
                product.publish();
            }
            case UNPUBLISHED -> {
                product.submit();
                product.publish();
                product.unpublish();
            }
            case DELETED -> {
                product.submit();
                product.publish();
                product.unpublish();
                product.delete();
            }
            default -> {
            }
        }
    }
}
