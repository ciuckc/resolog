package com.resolog.catalog.api.request;

import com.resolog.catalog.api.ProductStatusView;
import com.resolog.catalog.domain.model.ProductType;


public record ListProductsRequest (

    ProductStatusView status,

    ProductType type

) { }
