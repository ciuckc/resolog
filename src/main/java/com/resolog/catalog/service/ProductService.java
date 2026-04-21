package com.resolog.catalog.service;

import com.resolog.catalog.api.ProductStatusView;
import com.resolog.catalog.api.mapper.CatalogMapper;
import com.resolog.catalog.api.request.CreateProductRequest;
import com.resolog.catalog.api.request.UpdateProductRequest;
import com.resolog.catalog.api.response.GetProductResponse;
import com.resolog.catalog.domain.model.Artist;
import com.resolog.catalog.domain.model.Product;
import com.resolog.catalog.domain.model.ProductStatus;
import com.resolog.catalog.domain.model.ProductType;
import com.resolog.catalog.domain.repository.ArtistRepository;
import com.resolog.catalog.domain.repository.ProductRepository;
import com.resolog.catalog.domain.repository.TrackRepository;
import com.resolog.catalog.domain.specs.ProductSpecs;
import com.resolog.catalog.messaging.event.ProductCreated;
import com.resolog.catalog.messaging.event.ProductDeleted;
import com.resolog.catalog.messaging.event.ProductPublished;
import com.resolog.catalog.messaging.event.ProductRejected;
import com.resolog.catalog.messaging.event.ProductSubmittedForPublishing;
import com.resolog.catalog.messaging.event.ProductUnpublished;
import com.resolog.catalog.messaging.event.TrackPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProductService {

    private final ArtistRepository artistRepository;
    private final OutboxEventService outboxEventService;
    private final ProductRepository productRepository;
    private final TrackRepository trackRepository;

    public ProductService(
            ArtistRepository artistRepository,
            OutboxEventService outboxEventService,
            ProductRepository productRepository,
            TrackRepository trackRepository) {
        this.artistRepository = artistRepository;
        this.outboxEventService = outboxEventService;
        this.trackRepository = trackRepository;
        this.productRepository = productRepository;
    }

    public List<GetProductResponse> listActiveProducts(ProductStatusView status, ProductType type) {
        Specification<Product> specification = ProductSpecs.notDeleted();

        if (status != null) {
            specification = specification.and(ProductSpecs.hasStatus(ProductStatus.valueOf(status.name())));
        }
        if (type != null) {
            specification = specification.and(ProductSpecs.hasType(type));
        }

        return productRepository.findAll(specification).stream()
                .map(CatalogMapper::toDto)
                .toList();
    }

    public GetProductResponse getActiveProduct(UUID id) {
        return CatalogMapper.toDto(getActiveEntityById(id));
    }

    @Transactional
    public GetProductResponse createProduct(CreateProductRequest request) {
        if (request.artistIds() == null || request.artistIds().isEmpty()) {
            throw new IllegalArgumentException("Product must have at least one artist");
        }

        Product product = Product.create(
                request.productType(),
                request.title(),
                request.genre(),
                request.price(),
                request.releaseDate()
        );
        product.updateArtworkUrl(request.artworkUrl());
        resolveArtists(request.artistIds()).forEach(product::addArtist);

        Product saved = productRepository.save(product);
        log.info("Product {} created", saved.getId());

        outboxEventService.publish(
                saved.getId(),
                new ProductCreated(
                        saved.getId(),
                        saved.getType(),
                        saved.getStatus(),
                        saved.getTitle(),
                        saved.getGenre(),
                        saved.getReleaseDate(),
                        saved.getArtworkUrl(),
                        saved.getPrice(),
                        artistIds(saved)
        ));

        return CatalogMapper.toDto(saved);
    }

    public GetProductResponse updateProduct(UUID id, UpdateProductRequest request) {
        Product product = getActiveEntityById(id);

        if (request.type() != null) {
            product.updateType(request.type());
        }
        if (request.title() != null) {
            product.updateTitle(request.title());
        }
        if (request.genre() != null) {
            product.updateGenre(request.genre());
        }
        if (request.price() != null) {
            product.updatePrice(request.price());
        }
        if (request.releaseDate() != null) {
            product.updateReleaseDate(request.releaseDate());
        }
        if (request.artworkUrl() != null) {
            product.updateArtworkUrl(request.artworkUrl());
        }

        return CatalogMapper.toDto(productRepository.save(product));
    }

    @Transactional
    public void deleteProduct(UUID id) {
        Product product = getActiveEntityById(id);
        product.delete();
        productRepository.save(product);
        log.info("Product {} deleted", product.getId());

        outboxEventService.publish(
                product.getId(),
                new ProductDeleted(
                        product.getId(),
                        product.getType(),
                        product.getStatus(),
                        product.getTitle(),
                        product.getGenre(),
                        product.getReleaseDate(),
                        product.getArtworkUrl(),
                        product.getPrice(),
                        artistIds(product)
        ));
    }

    @Transactional
    public GetProductResponse publishProduct(UUID id) {
        Product product =  getActiveEntityById(id);
        product.submit();

        Product savedProduct = productRepository.save(product);

        List<TrackPayload> tracks = trackRepository.findByProductId(savedProduct.getId()).stream()
                .map(track ->
                        new TrackPayload(
                                track.getTrackNumber(),
                                track.getAudioUrl()))
                .toList();

        log.info("Product {} submitted for publishing", savedProduct.getId());

        outboxEventService.publish(
                savedProduct.getId(),
                new ProductSubmittedForPublishing(
                        savedProduct.getId(),
                        savedProduct.getType(),
                        savedProduct.getStatus(),
                        savedProduct.getTitle(),
                        savedProduct.getGenre(),
                        savedProduct.getReleaseDate(),
                        savedProduct.getArtworkUrl(),
                        savedProduct.getPrice(),
                        artistIds(savedProduct),
                        tracks
        ));

        return CatalogMapper.toDto(savedProduct);
    }

    public GetProductResponse revertProductToDraft(UUID id) {
        Product product =  getActiveEntityById(id);
        product.redraft();
        return CatalogMapper.toDto(productRepository.save(product));
    }

    @Transactional
    public GetProductResponse unpublishProduct(UUID id) {
        Product product =  getActiveEntityById(id);
        product.unpublish();

        Product savedProduct = productRepository.save(product);

        log.info("Product {} unpublished", savedProduct.getId());

        outboxEventService.publish(
                savedProduct.getId(),
                new ProductUnpublished(
                savedProduct.getId(),
                savedProduct.getType(),
                savedProduct.getStatus(),
                savedProduct.getTitle(),
                savedProduct.getGenre(),
                savedProduct.getReleaseDate(),
                savedProduct.getArtworkUrl(),
                savedProduct.getPrice(),
                artistIds(savedProduct)
        ));

        return CatalogMapper.toDto(savedProduct);
    }

    @Transactional
    public GetProductResponse addArtistsToProduct(UUID id, Set<UUID> artistIds) {
        if (artistIds == null || artistIds.isEmpty()) {
            throw new IllegalArgumentException("Artist IDs cannot be empty");
        }
        Product product = getActiveEntityById(id);
        resolveArtists(artistIds).forEach(product::addArtist);
        return CatalogMapper.toDto(productRepository.save(product));
    }
    @Transactional
    public GetProductResponse removeArtistFromProduct(UUID id, Set<UUID> artistIds) {
        if (artistIds == null || artistIds.isEmpty()) {
            throw new IllegalArgumentException("Artist IDs cannot be empty");
        }

        Product product = getActiveEntityById(id);

        artistIds.stream()
                .map(artistId -> artistRepository.findById(artistId)
                        .orElseThrow(() -> new NoSuchElementException("No artist with id " + artistId)))
                .forEach(product::removeArtist);

        return CatalogMapper.toDto(productRepository.save(product));
    }

    @Transactional
    public void confirmPublished(UUID id) {
        Product product = getActiveEntityById(id);
        product.publish();
        Product saved = productRepository.save(product);
        log.info("Product {} published", saved.getId());

        outboxEventService.publish(
                saved.getId(),
                new ProductPublished(
                saved.getId(),
                saved.getType(),
                saved.getStatus(),
                saved.getTitle(),
                saved.getGenre(),
                saved.getReleaseDate(),
                saved.getArtworkUrl(),
                saved.getPrice(),
                artistIds(saved)
        ));
    }

    @Transactional
    public void rejectProduct(UUID id, String reason) {
        Product product = getActiveEntityById(id);
        product.reject(reason);
        Product saved = productRepository.save(product);
        log.info("Product {} rejected - {}", saved.getId(), reason);

        outboxEventService.publish(
                saved.getId(),
                new ProductRejected(
                        saved.getId(),
                        saved.getType(),
                        saved.getStatus(),
                        saved.getTitle(),
                        saved.getGenre(),
                        saved.getReleaseDate(),
                        saved.getArtworkUrl(),
                        saved.getPrice(),
                        saved.getStatusReason(),
                        artistIds(saved)
        ));
    }

    private List<Artist> resolveArtists(Set<UUID> artistIds) {
        return artistIds.stream()
                .map(id -> artistRepository.findById(id)
                        .orElseThrow(() ->
                                new NoSuchElementException("No artist with id " + id)))
                .toList();
    }

    private Set<UUID> artistIds(Product product) {
        return product.getArtists().stream()
                .map(Artist::getId)
                .collect(Collectors.toSet());
    }

    private Product getActiveEntityById(UUID id) {
        return productRepository.findById(id)
                .filter(product -> product.getStatus() != ProductStatus.DELETED)
                .orElseThrow(() -> new NoSuchElementException("Product not found"));
    }

}
