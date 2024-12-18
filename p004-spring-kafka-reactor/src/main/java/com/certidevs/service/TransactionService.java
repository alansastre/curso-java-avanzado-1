package com.certidevs.service;

import com.certidevs.entity.Transaction;
import com.certidevs.repository.TransactionRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@AllArgsConstructor
@Service
public class TransactionService {

    private TransactionRepository transactionRepository;
    private ReactiveKafkaProducerTemplate<String, Transaction> transactionProducer;

    // kafka normal:
    //    private KafkaTemplate




    // Producer
    public Mono<Transaction> create(Transaction transaction) {
        transaction.setTimestamp(LocalDateTime.now());

        return transactionRepository.save(transaction).flatMap(savedTransaction -> {
            log.info("Creada transaction {}", transaction);
            // Producer. envía a kafka una transaction
            return transactionProducer.send("topic-transactions", savedTransaction.getId().toString(), savedTransaction).thenReturn(savedTransaction);
        });
    }
}
