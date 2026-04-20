package com.resolog.catalog.api.controller;

import com.resolog.catalog.api.request.AddArtistsToProductRequest;
import com.resolog.catalog.api.request.CreateProductRequest;
import com.resolog.catalog.api.request.ListProductsRequest;
import com.resolog.catalog.api.request.RemoveArtistsFromProduct;
import com.resolog.catalog.api.request.UpdateProductRequest;
import com.resolog.catalog.api.response.GetProductResponse;
import com.resolog.catalog.api.response.ListProductsResponse;
import com.resolog.catalog.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(
            summary = "List active products",
            description = "Returns all non-deleted products. " +
                    "Supports optional filtering by status and type.")
    @GetMapping
    public ListProductsResponse listProducts(@ModelAttribute ListProductsRequest request) {
        return new ListProductsResponse(
                productService.listActiveProducts(
                        request.status(),
                        request.type()));
    }

    @Operation(
            summary = "Get product by Id",
            description = "Returns a single active product. Returns 404 if not found or deleted.")
    @GetMapping("/{id}")
    public GetProductResponse getProduct(@PathVariable UUID id) {
        return productService.getActiveProduct(id);
    }

    @Operation(summary = "Create product", description = "Creates a new product in DRAFT status.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GetProductResponse createProduct(@RequestBody CreateProductRequest request) {
        return productService.createProduct(request);
    }

    @Operation(
            summary = "Update product",
            description = "Partially updates a product. " +
                    "Status cannot be changed via this endpoint.")
    @PatchMapping("/{id}")
    public GetProductResponse updateProduct(@PathVariable UUID id, @RequestBody UpdateProductRequest request) {
        return productService.updateProduct(id, request);
    }

    @Operation(
            summary = "Delete product",
            description = "Deletes a product. " +
                    "Only DRAFT or UNPUBLISHED products can be deleted.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable UUID id) {
        productService.deleteProduct(id);
    }

    @Operation(
            summary = "Submit product for publishing",
            description = "Transitions product from DRAFT to PUBLISHING. " +
                    "The system will asynchronously publish or reject it.")
    @PostMapping("/{id}/publish")
    public GetProductResponse publishProduct(@PathVariable UUID id) {
        return productService.publishProduct(id);
    }

    @Operation(
            summary = "Revert product to draft",
            description = "Transitions product from UNPUBLISHED back to DRAFT.")
    @PostMapping("/{id}/revert")
    public GetProductResponse revertProductToDraft(@PathVariable UUID id) {
        return productService.revertProductToDraft(id);
    }

    @Operation(
            summary = "Unpublish product",
            description = "Transitions product from PUBLISHED to UNPUBLISHED.")
    @PostMapping("/{id}/unpublish")
    public GetProductResponse unpublishProduct(@PathVariable UUID id) {
        return productService.unpublishProduct(id);
    }

    @Operation(
            summary = "Add artists to product",
            description = "Links one or more artists to a product. " +
                    "If any artist Id is invalid the operation rolls back.")
    @PostMapping("/{id}/artists")
    public GetProductResponse addArtistsToProduct(
            @PathVariable UUID id,
            @RequestBody AddArtistsToProductRequest request) {
        return productService.addArtistsToProduct(id, request.artistIds());
    }

    @Operation(
            summary = "Remove artists from product",
            description = "Unlinks one or more artists from a product." +
                    "If any artist Id is invalid the operation rolls back.")
    @PostMapping("/{id}/artists/remove")
    public GetProductResponse removeArtistFromProduct(
            @PathVariable UUID id,
            @RequestBody RemoveArtistsFromProduct request) {
        return productService.removeArtistFromProduct(id, request.artistIds());
    }
}
