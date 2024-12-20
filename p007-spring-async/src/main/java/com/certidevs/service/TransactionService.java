package com.certidevs.service;

import com.certidevs.dto.Transaction;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class TransactionService {


    public CompletableFuture<List<Transaction>> findAllTransactionsByUserId(Long userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Simula un retraso en la llamada externa
                TimeUnit.SECONDS.sleep(2);
                return List.of(
                        new Transaction(userId, 1L, 100.0, "deposit"),
                        new Transaction(userId, 2L, 200.0, "deposit"),
                        new Transaction(userId, 3L, 300.0, "deposit"),
                        new Transaction(userId, 4L, 300.0, "deposit")
                );
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        });
    }
}
