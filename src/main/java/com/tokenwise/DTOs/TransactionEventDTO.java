package com.tokenwise.DTOs;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionEventDTO {
    private String signature;
    private Instant blockTime;
    private String walletAddress;
    private String mint;
    private String source;
    private String destination;
    private String amount;
    private String protocol;
    private String buySell; // "buy", "sell", or "transfer"

}
