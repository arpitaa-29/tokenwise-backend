package com.tokenwise.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WalletFrequencyDTO {
    private String walletAddress;
    private Long frequency;
}