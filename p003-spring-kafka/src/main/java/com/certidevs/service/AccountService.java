package com.certidevs.service;

import com.certidevs.entity.Account;
import com.certidevs.entity.Transaction;
import com.certidevs.repository.AccountRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@AllArgsConstructor
@Service
public class AccountService {

    private AccountRepository accountRepository;

    public Optional<Account> findById(Long id) {
        return accountRepository.findById(id);
    }

    public Account create(Account account) {
        account.setBalance(0.0);
        return accountRepository.save(account);
    }

    // consumer KafkaListener
    @KafkaListener(topics = {"topic-transactions"}, groupId = "group-transactions")
    public void updateAccountBalance(Transaction transaction) {
        log.info("Transaction Consumer, recibida transaction {}", transaction);
        // Actualizar el balance de la cuenta

        Account account = accountRepository.findById(transaction.getAccountId()).orElse(null);
        if (account == null) {
            log.warn("Cuenta no encontrada {}",transaction.getAccountId() );
            return;
        }

        // se puede mejorar con un patrón de diseño Strategy
        switch (transaction.getType().toLowerCase()) {
            case "deposit" -> account.setBalance(account.getBalance() + transaction.getAmount());
            case "withdraw" -> account.setBalance(account.getBalance() - transaction.getAmount());
            default -> log.warn("Tipo de transacción desconocido {}", transaction.getType());
        }
        accountRepository.save(account);
        // opcional, se puede enviar a kafka un aviso como por ejemplo una notificación para que otro
        // microservicio envíe un correo al usuario o un sms avisando de como fue la transacción
//        kafkaTemplate.send...

        log.info("Balance actualizado {}: {}", account.getId(), account.getBalance());
    }

}
