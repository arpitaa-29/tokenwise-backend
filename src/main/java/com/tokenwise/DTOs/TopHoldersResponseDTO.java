package com.tokenwise.DTOs;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopHoldersResponseDTO {
    private String owner;           // Wallet address
    private long tokenQuantity;     // Raw token amount (sum of all token accounts)
    private String balance;
}
