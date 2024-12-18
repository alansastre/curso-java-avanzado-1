package com.certidevs.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "transaction")
public class Transaction {
    @Id
    private Long id;
    private Long accountId;
    private String type; // "deposit", "withdraw"
    private Double amount;
    private LocalDateTime timestamp;

}