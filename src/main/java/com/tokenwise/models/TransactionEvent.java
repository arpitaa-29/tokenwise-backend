package com.tokenwise.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Table(name = "transaction_events")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String signature;
    private Instant blockTime;
    private String walletAddress;
    private String mint;
    private String source;
    private String destination;
    private String amount;
    private String protocol;
    private String buySell;
}
