package com.certidevs.dto;

public record Order(
        Long userId,
        Long orderId,
        Double amount,
        String ticker
) {
}
