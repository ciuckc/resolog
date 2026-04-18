package com.resolog.catalog.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.UUID;
import java.util.Set;

@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = DbSchema.Products.TABLE)
public class Product extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ProductType type;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String genre;

    @Column(nullable = false, name = DbSchema.Products.RELEASE_DATE)
    private LocalDate releaseDate;

    @Column(name = DbSchema.Products.ARTWORK_URL)
    private String artworkUrl;

    @Column(name = DbSchema.Products.STATUS_REASON)
    private String statusReason;

    private BigDecimal price;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = DbSchema.Products.PRODUCT_ARTIST_TABLE,
            joinColumns = @JoinColumn(name = DbSchema.Products.ID),
            inverseJoinColumns = @JoinColumn(name = DbSchema.Products.ARTIST_ID)
    )
    @ToString.Exclude
    private Set<Artist> artists = new HashSet<>();

    public static Product create(
            ProductType type,
            @NonNull String title,
            @NonNull String genre,
            BigDecimal price,
            @NonNull LocalDate releaseDate) {
        Product product = new Product();
        product.updateType(type);
        product.updateTitle(title);
        product.updateGenre(genre);
        product.updatePrice(price);
        product.updateReleaseDate(releaseDate);
        product.status = ProductStatus.DRAFT;
        return product;
    }

    public void updateType(@NonNull ProductType type) {
        this.type = type;
    }

    public void updateTitle(@NonNull String title) {
        if (title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be blank");
        }
        this.title = title;
    }

    public void updateGenre(@NonNull String genre) {
        if (genre.isBlank()) {
            throw new IllegalArgumentException("Genre cannot be blank");
        }
        this.genre = genre;
    }

    public void updatePrice(BigDecimal price) {
        if (price != null && price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        this.price = price;
    }

    public void updateReleaseDate(@NonNull LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    // Little state machine helper since we don't want to expose the status directly without checking it

    public void redraft() {
        if (this.status == ProductStatus.DRAFT) {
            return;
        }
        if (this.status != ProductStatus.PUBLISHING && this.status != ProductStatus.UNPUBLISHED) {
            throw new IllegalStateException("Product must be in publishing or unpublished status" +
                    " to be marked as draft");
        }
        this.status = ProductStatus.DRAFT;
    }

    public void submit() {
        if (this.status == ProductStatus.PUBLISHING) {
            return;
        }
        if (this.status != ProductStatus.DRAFT) {
            throw new IllegalStateException("Product must be in draft status to be published");
        }
        this.status = ProductStatus.PUBLISHING;
        this.statusReason = null;
    }

    public void publish() {
        if (this.status == ProductStatus.PUBLISHED) {
            return;
        }
        if (this.status != ProductStatus.PUBLISHING) {
            throw new IllegalStateException("Product must be in publishing status to be published");
        }
        this.status = ProductStatus.PUBLISHED;
    }

    public void unpublish() {
        if (this.status == ProductStatus.UNPUBLISHED) {
            return;
        }
        if (this.status != ProductStatus.PUBLISHED) {
            throw new IllegalStateException("Product must be in published status to be unpublished");
        }
        this.status = ProductStatus.UNPUBLISHED;
    }

    public void delete() {
        if (this.status != ProductStatus.UNPUBLISHED && this.status != ProductStatus.DRAFT) {
            throw new IllegalStateException("Product must be unpublished or in draft staus in order to be deleted");
        }
        this.status = ProductStatus.DELETED;
    }

    public void reject(@NonNull String reason) {
        if (this.status != ProductStatus.PUBLISHING) {
            throw new IllegalStateException("Can only reject product a product that's currently publishing");
        }
        if (reason.isBlank()) {
            throw new IllegalArgumentException("Reason cannot be blank");
        }
        this.status = ProductStatus.DRAFT;
        this.statusReason = reason;
    }
}
