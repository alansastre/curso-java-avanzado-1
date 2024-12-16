package com.certidevs.dto;

import java.util.List;

public record PaginatedResponseRecord<T>(
        List<T> items,
        Integer page,
        Integer size,
        Long total
) {
}
