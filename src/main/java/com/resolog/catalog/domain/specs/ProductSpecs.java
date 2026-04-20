package com.resolog.catalog.domain.specs;

import com.resolog.catalog.domain.model.Product;
import com.resolog.catalog.domain.model.ProductStatus;
import com.resolog.catalog.domain.model.ProductType;
import org.springframework.data.jpa.domain.Specification;

public final class ProductSpecs {

    public static Specification<Product> notDeleted() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.notEqual(root.get("status"), ProductStatus.DELETED);
    }

    public static Specification<Product> hasStatus(ProductStatus status) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<Product> hasType(ProductType type) { return  (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("type"), type);
    }

    private ProductSpecs() { }
}
