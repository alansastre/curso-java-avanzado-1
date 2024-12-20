package com.certidevs.dto;

import com.certidevs.entity.User;

import java.util.List;

public record UserConsolidated(
        User user,
        List<Order> orders,
        List<Transaction> transactions
) {
}
