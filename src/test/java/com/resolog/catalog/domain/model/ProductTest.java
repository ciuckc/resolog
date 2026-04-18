package com.resolog.catalog.domain.model;

import com.resolog.catalog.config.JpaConfig;
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

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    protected void setUp() {
        product = TestFixtures.aProduct();
        savedProduct = productRepository.saveAndFlush(product);
    }

    @Test
    public void testNullPtrCreateThrowsNPE() {
        assertThrows(NullPointerException.class, () -> product = Product.create(null, null, null, null, null));
    }

    @Test
    public void testProductIsSaved() {
        assertNotNull(savedProduct);
        assertEquals(product, savedProduct);
    }

    @Test
    public void testUpdateSavedType() {
        savedProduct.updateType(ProductType.ALBUM);
        final Product updatedProduct = productRepository.saveAndFlush(savedProduct);

        assertNotNull(updatedProduct);
        assertEquals(ProductType.ALBUM, updatedProduct.getType());
    }

    @Test
    public void testUpdateSavedTitleThrowsWhenEmpty() {
        assertThrows(IllegalArgumentException.class, () -> savedProduct.updateTitle(""));
    }

    @Test
    public void testUpdateSavedTitleThrowsWhenHasSpacesAndNewLine() {
        assertThrows(IllegalArgumentException.class, () -> savedProduct.updateTitle("   \n"));
    }

    @Test
    public void testUpdateGenreThrowsWhenEmpty() {
        assertThrows(IllegalArgumentException.class, () -> savedProduct.updateGenre(""));
    }

    @Test
    public void testUpdateGenreThrowsWhenHasSpacesAndNewLine() {
        assertThrows(IllegalArgumentException.class, () -> savedProduct.updateGenre("   \n"));
    }

    @Test
    public void testUpdatePriceThrowsWhenNegative() {
        assertThrows(IllegalArgumentException.class, () -> savedProduct.updatePrice(BigDecimal.valueOf(-10.0)));
    }

    @Test
    public void testUpdateReleaseDateThrowsWhenNull() {
        assertThrows(NullPointerException.class, () -> savedProduct.updateReleaseDate(null));
    }

    @Test
    public void testAllTransitions() {
        assertDoesNotThrow(() -> savedProduct.markAsDraft());
        assertDoesNotThrow(() -> savedProduct.markAsPublishing());
        assertDoesNotThrow(() -> savedProduct.publish());
        assertDoesNotThrow(() -> savedProduct.markAsUnpublished());
        assertDoesNotThrow(() -> savedProduct.markAsDraft());
    }

    @Test
    public void testInitialStatusIsDraft() {
        assertEquals(ProductStatus.DRAFT, savedProduct.getStatus());
    }

    @Test
    public void testMarkAsDraftNoOp() {
        assertDoesNotThrow(() -> savedProduct.markAsDraft());
    }

    @Test
    public void testMarkAsDraftThrowsWhenPublished() {
        transitionStatusTo(savedProduct, ProductStatus.PUBLISHED);
        assertThrows(IllegalStateException.class, () -> savedProduct.markAsDraft());
    }

    @Test
    public void testMarkAsDraftThrowsWhenDeleted() {
        transitionStatusTo(savedProduct, ProductStatus.DELETED);
        assertThrows(IllegalStateException.class, () -> savedProduct.markAsDraft());
    }

    @Test
    public void testMarkAsPublishingNoOp() {
        transitionStatusTo(savedProduct, ProductStatus.PUBLISHING);
        assertDoesNotThrow(() -> savedProduct.markAsPublishing());
    }

    @Test
    public void testMarkAsPublishingThrowsWhenPublished() {
        transitionStatusTo(savedProduct, ProductStatus.PUBLISHED);
        assertThrows(IllegalStateException.class, () -> savedProduct.markAsPublishing());
    }

    @Test
    public void testMarkAsPublishingThrowsWhenDeleted() {
        transitionStatusTo(savedProduct, ProductStatus.DELETED);
        assertThrows(IllegalStateException.class, () -> savedProduct.markAsPublishing());
    }

    @Test
    public void testPublishNoOp() {
        transitionStatusTo(savedProduct, ProductStatus.PUBLISHED);
        assertDoesNotThrow(() -> savedProduct.publish());
    }

    @Test
    public void testPublishThrowsWhenDraft() {
        assertThrows(IllegalStateException.class, () -> savedProduct.publish());
    }

    @Test
    public void testPublishThrowsWhenUnpublished() {
        transitionStatusTo(savedProduct, ProductStatus.UNPUBLISHED);
        assertThrows(IllegalStateException.class, () -> savedProduct.publish());
    }

    @Test
    public void testPublishThrowsWhenDeleted() {
        transitionStatusTo(savedProduct, ProductStatus.DELETED);
        assertThrows(IllegalStateException.class, () -> savedProduct.publish());
    }

    @Test
    public void testDeleteThrowsWhenPublished() {
        transitionStatusTo(savedProduct, ProductStatus.PUBLISHED);
        assertThrows(IllegalStateException.class, () -> savedProduct.delete());
    }

    @Test
    public void testDeleteThrowsWhenPublishing() {
        transitionStatusTo(savedProduct, ProductStatus.PUBLISHING);
        assertThrows(IllegalStateException.class, () -> savedProduct.delete());
    }

    @Test
    public void testFindSavedProductById() {
        Product foundProduct = productRepository.findById(savedProduct.getId()).orElse(null);
        assertNotNull(foundProduct);
        assertEquals(savedProduct.getId(), foundProduct.getId());
        assertEquals(savedProduct.getTitle(), foundProduct.getTitle());
        assertEquals(savedProduct.getGenre(), foundProduct.getGenre());
        assertEquals(savedProduct.getPrice(), foundProduct.getPrice());
        assertEquals(savedProduct.getStatus(), foundProduct.getStatus());
    }

    @Test
    public void testRejectThrowsWhenNotPublishing() {
        assertThrows(IllegalStateException.class, () -> product.reject("hello"));
    }

    @Test
    public void testRejectThrowsWhenReasonIsBlank() {
        transitionStatusTo(savedProduct, ProductStatus.PUBLISHING);
        assertThrows(IllegalArgumentException.class, () -> product.reject(""));
    }

    @Test
    public void testRejectCompletesWhenPublishing() {
        transitionStatusTo(savedProduct, ProductStatus.PUBLISHING);
        assertDoesNotThrow(() -> savedProduct.reject("hello"));
    }

    private void transitionStatusTo(Product product, ProductStatus type) {
        switch (type) {
            case PUBLISHING -> product.markAsPublishing();
            case PUBLISHED -> {
                product.markAsPublishing();
                product.publish();
            }
            case UNPUBLISHED -> {
                product.markAsPublishing();
                product.publish();
                product.markAsUnpublished();
            }
            case DELETED -> {
                product.markAsPublishing();
                product.publish();
                product.markAsUnpublished();
                product.delete();
            }
            default -> {
            }
        }
    }
}
