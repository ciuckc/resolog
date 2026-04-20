package com.resolog.catalog.api.response;

import java.util.List;

public record ListProductsResponse(

        List<GetProductResponse> products

) { }
