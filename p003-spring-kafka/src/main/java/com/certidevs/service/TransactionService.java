package com.certidevs.service;

import com.certidevs.entity.Transaction;
import com.certidevs.repository.TransactionRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@AllArgsConstructor
@Service
public class TransactionService {

    private TransactionRepository transactionRepository;
    private KafkaTemplate<String, Transaction> transactionProducer;

    public Transaction create(Transaction transaction) {
        transaction.setTimestamp(LocalDateTime.now());
        transactionRepository.save(transaction);
        // enviar a kafka usando template
        transactionProducer.sendDefault(transaction);
        log.info("Transaction Producer, enviada transaction {}", transaction);
        // transactionProducer.send("topic-transactions-risk", transaction);
        // transactionProducer.send("topic-transactions-risk", transaction.getId().toString(), transaction);
        return transaction;
    }
}
