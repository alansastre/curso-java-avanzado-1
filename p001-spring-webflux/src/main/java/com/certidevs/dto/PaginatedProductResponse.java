package com.certidevs.dto;

import com.certidevs.entity.Product;

import java.util.List;

public record PaginatedProductResponse(
        List<Product> products,
        Integer page,
        Integer size,
        Long total
) {
}
