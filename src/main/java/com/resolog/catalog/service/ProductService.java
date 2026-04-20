package com.resolog.catalog.service;

import com.resolog.catalog.api.ProductStatusView;
import com.resolog.catalog.api.mapper.CatalogMapper;
import com.resolog.catalog.api.request.CreateProductRequest;
import com.resolog.catalog.api.request.UpdateProductRequest;
import com.resolog.catalog.api.response.GetProductResponse;
import com.resolog.catalog.domain.model.Product;
import com.resolog.catalog.domain.model.ProductStatus;
import com.resolog.catalog.domain.model.ProductType;
import com.resolog.catalog.domain.repository.ArtistRepository;
import com.resolog.catalog.domain.repository.ProductRepository;
import com.resolog.catalog.domain.specs.ProductSpecs;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

@Service
public class ProductService {

    private final ArtistRepository artistRepository;

    private final ProductRepository productRepository;

    public ProductService(ArtistRepository artistRepository, ProductRepository productRepository) {
        this.artistRepository = artistRepository;
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

    public GetProductResponse createProduct(CreateProductRequest request) {
        Product product = Product.create(
                request.productType(),
                request.title(),
                request.genre(),
                request.price(),
                request.releaseDate()
        );
        product.updateArtworkUrl(request.artworkUrl());

        return CatalogMapper.toDto(productRepository.save(product));
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

    public void deleteProduct(UUID id) {
        Product product =  getActiveEntityById(id);
        product.delete();
        productRepository.save(product);
    }

    public GetProductResponse publishProduct(UUID id) {
        Product product =  getActiveEntityById(id);
        product.submit();
        return CatalogMapper.toDto(productRepository.save(product));
    }

    public GetProductResponse revertProductToDraft(UUID id) {
        Product product =  getActiveEntityById(id);
        product.redraft();
        return CatalogMapper.toDto(productRepository.save(product));
    }

    public GetProductResponse unpublishProduct(UUID id) {
        Product product =  getActiveEntityById(id);
        product.unpublish();
        return CatalogMapper.toDto(productRepository.save(product));
    }

    @Transactional
    public GetProductResponse addArtistsToProduct(UUID id, Set<UUID> artistIds) {
        if (artistIds == null ||  artistIds.isEmpty()) {
            throw new NoSuchElementException("Artist IDs cannot be empty");
        }
        Product product = getActiveEntityById(id);

        artistIds.stream()
                .map(artistId ->
                        artistRepository.findById(artistId)
                                .orElseThrow(() ->
                                        new NoSuchElementException("No artist with id " + artistId)))
                .forEach(product::addArtist);
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

    // Little helper to not repeat this logic

    private Product getActiveEntityById(UUID id) {
        return productRepository.findById(id)
                .filter(product -> product.getStatus() != ProductStatus.DELETED)
                .orElseThrow(() -> new NoSuchElementException("Product not found"));
    }

}
