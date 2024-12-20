package com.certidevs.dto;

public record Transaction(
        Long userId,
        Long transactionId,
        Double amount,
        String type
) {
}
