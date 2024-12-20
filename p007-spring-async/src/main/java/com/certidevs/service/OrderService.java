package com.certidevs.service;

import com.certidevs.dto.Order;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class OrderService {

    public CompletableFuture<List<Order>> findAllOrdersByUserId(Long userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Simula un retraso en la llamada externa
                TimeUnit.SECONDS.sleep(2);
                return List.of(
                        new Order(userId, 1L, 100.0, "BTC"),
                        new Order(userId, 2L, 100.0, "BTC"),
                        new Order(userId, 3L, 100.0, "BTC"),
                        new Order(userId, 4L, 100.0, "BTC")
                );
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        });
    }
}
