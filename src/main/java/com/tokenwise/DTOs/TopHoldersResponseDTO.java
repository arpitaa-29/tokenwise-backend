package com.tokenwise.DTOs;

public class TopHoldersResponseDTO {
    private String owner;           // Wallet address
    private long tokenQuantity;     // Raw token amount (sum of all token accounts)
    private String balance;         // Human-readable balance (formatted with decimals)

    public TopHoldersResponseDTO(String owner, long tokenQuantity, String balance) {
        this.owner = owner;
        this.tokenQuantity = tokenQuantity;
        this.balance = balance;
    }

    // Getters and Setters
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public long getTokenQuantity() {
        return tokenQuantity;
    }

    public void setTokenQuantity(long tokenQuantity) {
        this.tokenQuantity = tokenQuantity;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }
}
