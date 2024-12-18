package com.certidevs.service;

import com.certidevs.entity.Account;
import com.certidevs.entity.Transaction;
import com.certidevs.repository.AccountRepository;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Slf4j
@AllArgsConstructor
@Service
public class AccountService {

    private AccountRepository accountRepository;
    private ReactiveKafkaConsumerTemplate<String, Transaction> transactionConsumer;

    public Mono<Account> findById(Long id) {
        return accountRepository.findById(id);
    }

    public Mono<Account> create(Account account) {
        account.setBalance(0.0);
        return accountRepository.save(account);
    }

    // consumer
    @PostConstruct // cuando termine de inicializar la clase se inicia el consumer
    public void startConsumer() {
        transactionConsumer.receiveAutoAck()
                .map(record -> record.value())
                .flatMap(this::updateAccountBalance)
                .doOnError(e -> log.error("Error procesando transaction", e))
//                .limitRate(100)
//                .buffer(100)
//                .retry(3)
//                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(2)))
                // inicia con 2s y aumenta hasta un máximo de 10s de espera
//                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2)).maxBackoff(Duration.ofSeconds(10)))
                .onErrorContinue((e, o) -> log.error("Continuando después de error", e))
                .subscribe(unused -> log.info("subscripción consumer de transactions"), e -> log.error("Error en la suscripción", e));
    }

    public Mono<Void> updateAccountBalance(Transaction transaction) {
        return accountRepository.findById(transaction.getAccountId()).flatMap(account -> {
            log.info("Procesando transaction {} para actualizar account {}", transaction, account);
            switch (transaction.getType().toLowerCase()) {
                case "deposit" -> account.setBalance(account.getBalance() + transaction.getAmount());
                case "withdraw" -> account.setBalance(account.getBalance() - transaction.getAmount());
                default -> log.warn("Tipo de transacción desconocido {}", transaction.getType());
            }
            return accountRepository.save(account);
        }).doOnSuccess(account -> log.info("Balance actualizado en account {}", account))
        .then();
    }
}
