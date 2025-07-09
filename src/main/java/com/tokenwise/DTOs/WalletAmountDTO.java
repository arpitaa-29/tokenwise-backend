package com.tokenwise.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WalletAmountDTO {
    private String wallet;
    private double amount;
}
